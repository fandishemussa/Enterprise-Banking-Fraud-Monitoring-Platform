package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FraudRuleController {

    private final FraudRuleService fraudRuleService;

    @GetMapping("/api/v1/fraud-rules")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR', 'VIEWER', 'TESTER')")
    public ApiResponse<List<FraudRuleResponse>> getAll() {
        return ApiResponse.success(fraudRuleService.getAll());
    }

    @GetMapping("/api/v1/fraud-rules/{ruleId}")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR', 'VIEWER', 'TESTER')")
    public ApiResponse<FraudRuleResponse> getByRuleId(@PathVariable String ruleId) {
        return ApiResponse.success(fraudRuleService.getByRuleId(ruleId));
    }

    @PostMapping("/api/v1/fraud-rules")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<FraudRuleResponse> create(@Valid @RequestBody FraudRuleRequest request, Authentication authentication) {
        return ApiResponse.success("Fraud rule created", fraudRuleService.create(request, authentication.getName()));
    }

    @PutMapping("/api/v1/fraud-rules/{ruleId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<FraudRuleResponse> update(@PathVariable String ruleId, @Valid @RequestBody FraudRuleRequest request,
                                                  Authentication authentication) {
        return ApiResponse.success("Fraud rule updated", fraudRuleService.update(ruleId, request, authentication.getName()));
    }

    @PatchMapping("/api/v1/fraud-rules/{ruleId}/enable")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<FraudRuleResponse> enable(@PathVariable String ruleId, Authentication authentication) {
        return ApiResponse.success("Fraud rule enabled", fraudRuleService.setEnabled(ruleId, true, authentication.getName()));
    }

    @PatchMapping("/api/v1/fraud-rules/{ruleId}/disable")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<FraudRuleResponse> disable(@PathVariable String ruleId, Authentication authentication) {
        return ApiResponse.success("Fraud rule disabled", fraudRuleService.setEnabled(ruleId, false, authentication.getName()));
    }

    @GetMapping("/api/v1/fraud-rules/{ruleId}/versions")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR', 'VIEWER', 'TESTER')")
    public ApiResponse<List<FraudRuleVersionResponse>> getVersions(@PathVariable String ruleId) {
        return ApiResponse.success(fraudRuleService.getVersions(ruleId));
    }
}
