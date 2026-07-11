package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BulkOperationResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class InvestigationCaseController {

    private final InvestigationCaseService investigationCaseService;

    @PostMapping
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST')")
    public ApiResponse<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request, Authentication authentication) {
        return ApiResponse.success("Case created",
                investigationCaseService.createCase(request, authentication.getName(), roleOf(authentication)));
    }

    @GetMapping
    public ApiResponse<List<CaseResponse>> getAllCases(Authentication authentication) {
        return ApiResponse.success(
                investigationCaseService.getCasesVisibleTo(authentication.getName(), roleOf(authentication)));
    }

    @GetMapping("/{caseId}")
    public ApiResponse<CaseResponse> getCase(@PathVariable String caseId) {
        return ApiResponse.success(investigationCaseService.getCaseByPublicId(caseId));
    }

    @PatchMapping("/{caseId}")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseResponse> updateCase(@PathVariable String caseId,
                                                 @RequestBody UpdateCaseRequest request,
                                                 Authentication authentication) {
        return ApiResponse.success("Case updated",
                investigationCaseService.updateCase(caseId, request, authentication.getName(), roleOf(authentication)));
    }

    @PatchMapping("/bulk-update")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<BulkOperationResponse> bulkUpdate(@Valid @RequestBody BulkUpdateCasesRequest request,
                                                          Authentication authentication) {
        UpdateCaseRequest updateRequest = new UpdateCaseRequest(request.status(), request.assignedTo(), null);
        BulkOperationResponse result = BulkOperationResponse.forEach(
                request.caseIds(),
                caseId -> investigationCaseService.updateCase(
                        caseId, updateRequest, authentication.getName(), roleOf(authentication)));
        return ApiResponse.success(result.failures().isEmpty() ? "All cases updated" : "Some cases could not be updated", result);
    }

    @PatchMapping("/{caseId}/decision")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseResponse> updateDecision(@PathVariable String caseId,
                                                     @Valid @RequestBody UpdateCaseDecisionRequest request,
                                                     Authentication authentication) {
        return ApiResponse.success("Case decision updated", investigationCaseService.updateDecision(
                caseId, request.decision(), authentication.getName(), roleOf(authentication)));
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
    }
}
