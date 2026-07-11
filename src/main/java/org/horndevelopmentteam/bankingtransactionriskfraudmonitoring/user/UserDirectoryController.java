package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Deliberately outside /api/v1/users/**, which SecurityConfig locks to ADMIN only - this endpoint
 * needs to be reachable by ANALYST/INVESTIGATOR too so the alert assign/escalate dropdowns can be
 * populated without granting those roles full user-management access. */
@RestController
@RequiredArgsConstructor
public class UserDirectoryController {

    private final AppUserService appUserService;

    @GetMapping("/api/v1/directory/assignable-users")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'INVESTIGATOR')")
    public ApiResponse<List<AssignableUserResponse>> getAssignableUsers() {
        return ApiResponse.success(appUserService.getAssignableUsers());
    }
}
