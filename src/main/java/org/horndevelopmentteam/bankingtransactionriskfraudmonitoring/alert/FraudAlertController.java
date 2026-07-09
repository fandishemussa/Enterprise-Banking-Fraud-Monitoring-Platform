package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;
    private final CustomerService customerService;

    @GetMapping("/api/v1/alerts")
    public ApiResponse<List<FraudAlertResponse>> getAllAlerts() {
        return ApiResponse.success(fraudAlertService.getAllAlerts());
    }

    @GetMapping("/api/v1/alerts/{alertId}")
    public ApiResponse<FraudAlertResponse> getAlert(@PathVariable String alertId) {
        return ApiResponse.success(fraudAlertService.getAlertByPublicId(alertId));
    }

    @PatchMapping("/api/v1/alerts/{alertId}/status")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<FraudAlertResponse> updateStatus(@PathVariable String alertId,
                                                         @Valid @RequestBody UpdateAlertStatusRequest request) {
        return ApiResponse.success("Alert status updated", fraudAlertService.updateStatus(alertId, request.status()));
    }

    @PatchMapping("/api/v1/alerts/{alertId}/assign")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<FraudAlertResponse> assignAlert(@PathVariable String alertId,
                                                        @Valid @RequestBody UpdateAlertAssignRequest request) {
        return ApiResponse.success("Alert assigned", fraudAlertService.assign(alertId, request.assignedTo()));
    }

    @GetMapping("/api/v1/customers/{customerId}/alerts")
    public ApiResponse<List<FraudAlertResponse>> getAlertsForCustomer(@PathVariable String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        return ApiResponse.success(fraudAlertService.getAlertsForCustomer(customer));
    }
}
