package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ApiResponse<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest request) {
        return ApiResponse.success("Case created", investigationCaseService.createCase(request));
    }

    @GetMapping
    public ApiResponse<List<CaseResponse>> getAllCases() {
        return ApiResponse.success(investigationCaseService.getAllCases());
    }

    @GetMapping("/{caseId}")
    public ApiResponse<CaseResponse> getCase(@PathVariable String caseId) {
        return ApiResponse.success(investigationCaseService.getCaseByPublicId(caseId));
    }

    @PatchMapping("/{caseId}")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseResponse> updateCase(@PathVariable String caseId,
                                                 @RequestBody UpdateCaseRequest request) {
        return ApiResponse.success("Case updated", investigationCaseService.updateCase(caseId, request));
    }

    @PatchMapping("/{caseId}/decision")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseResponse> updateDecision(@PathVariable String caseId,
                                                     @Valid @RequestBody UpdateCaseDecisionRequest request) {
        return ApiResponse.success("Case decision updated", investigationCaseService.updateDecision(caseId, request.decision()));
    }
}
