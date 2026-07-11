package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases/{caseId}")
@RequiredArgsConstructor
public class CaseTimelineController {

    private final CaseTimelineService caseTimelineService;

    @GetMapping("/timeline")
    public ApiResponse<List<CaseTimelineEventResponse>> getTimeline(@PathVariable String caseId) {
        return ApiResponse.success(caseTimelineService.getTimeline(caseId));
    }

    @GetMapping("/notes")
    public ApiResponse<List<CaseNoteResponse>> getNotes(@PathVariable String caseId) {
        return ApiResponse.success(caseTimelineService.getNotes(caseId));
    }

    @PostMapping("/notes")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseNoteResponse> addNote(@PathVariable String caseId, @Valid @RequestBody CaseNoteRequest request,
                                                  Authentication authentication) {
        return ApiResponse.success("Note added", caseTimelineService.addNote(
                caseId, request, authentication.getName(), roleOf(authentication)));
    }

    @PutMapping("/notes/{noteId}")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<CaseNoteResponse> updateNote(@PathVariable String caseId, @PathVariable String noteId,
                                                     @Valid @RequestBody CaseNoteRequest request,
                                                     Authentication authentication) {
        return ApiResponse.success("Note updated", caseTimelineService.updateNote(
                caseId, noteId, request, authentication.getName(), roleOf(authentication)));
    }

    @DeleteMapping("/notes/{noteId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<Void> deleteNote(@PathVariable String caseId, @PathVariable String noteId,
                                         Authentication authentication) {
        caseTimelineService.deleteNote(caseId, noteId, authentication.getName());
        return ApiResponse.success("Note deleted", null);
    }

    @GetMapping("/status-history")
    public ApiResponse<List<CaseStatusHistoryResponse>> getStatusHistory(@PathVariable String caseId) {
        return ApiResponse.success(caseTimelineService.getStatusHistory(caseId));
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
    }
}
