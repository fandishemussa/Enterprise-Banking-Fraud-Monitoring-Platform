package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
public class AlertSlaController {

    private final AlertSlaService alertSlaService;

    @GetMapping("/summary")
    public ApiResponse<SlaSummaryResponse> getSummary() {
        return ApiResponse.success(alertSlaService.getSummary());
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertSlaResultResponse>> getAllSlaAlerts() {
        return ApiResponse.success(alertSlaService.getAllSlaResults());
    }

    @GetMapping("/breached")
    public ApiResponse<List<AlertSlaResultResponse>> getBreached() {
        return ApiResponse.success(alertSlaService.getBreached());
    }

    @GetMapping("/near-breach")
    public ApiResponse<List<AlertSlaResultResponse>> getNearBreach() {
        return ApiResponse.success(alertSlaService.getNearBreach());
    }

    @GetMapping("/policies")
    public ApiResponse<List<AlertSlaPolicyResponse>> getPolicies() {
        return ApiResponse.success(alertSlaService.getPolicies());
    }

    @PutMapping("/policies/{policyId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<AlertSlaPolicyResponse> updatePolicy(@PathVariable String policyId,
                                                             @Valid @RequestBody AlertSlaPolicyRequest request,
                                                             Authentication authentication) {
        return ApiResponse.success("SLA policy updated",
                alertSlaService.updatePolicy(policyId, request, authentication.getName()));
    }
}
