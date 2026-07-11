package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalationRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalationResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertSlaService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertStatusHistoryResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.InvestigationCaseService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BulkOperationResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.PageSupport;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;
    private final CustomerService customerService;
    private final AlertSlaService alertSlaService;
    private final InvestigationCaseService investigationCaseService;

    @GetMapping("/api/v1/alerts")
    public ResponseEntity<ApiResponse<List<FraudAlertResponse>>> getAllAlerts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication) {
        Pageable pageable = PageSupport.of(page, size, "createdAt");
        Page<FraudAlertResponse> result = fraudAlertService.getAlertsVisibleTo(
                authentication.getName(), roleOf(authentication), pageable);
        return PageSupport.withPageHeaders(
                ApiResponse.success(result.getContent()),
                result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @GetMapping("/api/v1/alerts/{alertId}")
    public ApiResponse<FraudAlertResponse> getAlert(@PathVariable String alertId) {
        return ApiResponse.success(fraudAlertService.getAlertByPublicId(alertId));
    }

    @PatchMapping("/api/v1/alerts/{alertId}/status")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<FraudAlertResponse> updateStatus(@PathVariable String alertId,
                                                         @Valid @RequestBody UpdateAlertStatusRequest request,
                                                         Authentication authentication) {
        FraudAlertResponse updated = fraudAlertService.updateStatus(alertId, request.status(), authentication.getName());
        if (request.status() == AlertStatus.INVESTIGATING) {
            investigationCaseService.ensureCaseForAlert(alertId, authentication.getName(), roleOf(authentication));
        }
        return ApiResponse.success("Alert status updated", updated);
    }

    @PatchMapping("/api/v1/alerts/{alertId}/assign")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<FraudAlertResponse> assignAlert(@PathVariable String alertId,
                                                        @Valid @RequestBody UpdateAlertAssignRequest request,
                                                        Authentication authentication) {
        FraudAlertResponse updated = fraudAlertService.assign(alertId, request.assignedTo());
        investigationCaseService.syncAssigneeForAlert(
                alertId, request.assignedTo(), authentication.getName(), roleOf(authentication));
        return ApiResponse.success("Alert assigned", updated);
    }

    @PatchMapping("/api/v1/alerts/bulk-assign")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<BulkOperationResponse> bulkAssign(@Valid @RequestBody BulkAssignAlertsRequest request,
                                                          Authentication authentication) {
        BulkOperationResponse result = BulkOperationResponse.forEach(request.alertIds(), alertId -> {
            fraudAlertService.assign(alertId, request.assignedTo());
            investigationCaseService.syncAssigneeForAlert(
                    alertId, request.assignedTo(), authentication.getName(), roleOf(authentication));
        });
        return ApiResponse.success(result.failures().isEmpty() ? "All alerts assigned" : "Some alerts could not be assigned", result);
    }

    @PatchMapping("/api/v1/alerts/bulk-status")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<BulkOperationResponse> bulkUpdateStatus(@Valid @RequestBody BulkUpdateAlertStatusRequest request,
                                                                Authentication authentication) {
        BulkOperationResponse result = BulkOperationResponse.forEach(request.alertIds(), alertId -> {
            fraudAlertService.updateStatus(alertId, request.status(), authentication.getName());
            if (request.status() == AlertStatus.INVESTIGATING) {
                investigationCaseService.ensureCaseForAlert(alertId, authentication.getName(), roleOf(authentication));
            }
        });
        return ApiResponse.success(result.failures().isEmpty() ? "All alerts updated" : "Some alerts could not be updated", result);
    }

    @PostMapping("/api/v1/alerts/{alertId}/escalate")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<AlertEscalationResponse> escalateAlert(@PathVariable String alertId,
                                                               @Valid @RequestBody AlertEscalationRequest request,
                                                               Authentication authentication) {
        AlertEscalationResponse response = fraudAlertService.escalate(alertId, request, authentication.getName());
        investigationCaseService.syncAssigneeForAlert(
                alertId, request.escalatedTo(), authentication.getName(), roleOf(authentication));
        return ApiResponse.success("Alert escalated", response);
    }

    @PostMapping("/api/v1/alerts/bulk-escalate")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<BulkOperationResponse> bulkEscalate(@Valid @RequestBody BulkEscalateAlertsRequest request,
                                                            Authentication authentication) {
        AlertEscalationRequest escalationRequest = new AlertEscalationRequest(request.escalatedTo(), request.reason());
        BulkOperationResponse result = BulkOperationResponse.forEach(request.alertIds(), alertId -> {
            fraudAlertService.escalate(alertId, escalationRequest, authentication.getName());
            investigationCaseService.syncAssigneeForAlert(
                    alertId, request.escalatedTo(), authentication.getName(), roleOf(authentication));
        });
        return ApiResponse.success(result.failures().isEmpty() ? "All alerts escalated" : "Some alerts could not be escalated", result);
    }

    @GetMapping("/api/v1/alerts/{alertId}/status-history")
    public ApiResponse<List<AlertStatusHistoryResponse>> getStatusHistory(@PathVariable String alertId) {
        return ApiResponse.success(alertSlaService.getStatusHistory(alertId));
    }

    @GetMapping("/api/v1/alerts/{alertId}/escalations")
    public ApiResponse<List<AlertEscalationResponse>> getEscalations(@PathVariable String alertId) {
        return ApiResponse.success(alertSlaService.getEscalations(alertId));
    }

    @GetMapping("/api/v1/customers/{customerId}/alerts")
    public ApiResponse<List<FraudAlertResponse>> getAlertsForCustomer(@PathVariable String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        return ApiResponse.success(fraudAlertService.getAlertsForCustomer(customer));
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
    }
}
