package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** ADMIN-only per SecurityConfig's requestMatcher on /api/v1/users/**; @PreAuthorize is kept too
 * so intent is explicit even if SECURITY_ENABLED=false for local testing. */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        return ApiResponse.success("User created", appUserService.createUser(request, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(appUserService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> getUser(@PathVariable String userId) {
        return ApiResponse.success(appUserService.getUserByPublicId(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request, Authentication authentication) {
        return ApiResponse.success("User updated", appUserService.updateUser(userId, request, authentication.getName()));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> updateStatus(@PathVariable String userId, @Valid @RequestBody UpdateUserStatusRequest request, Authentication authentication) {
        return ApiResponse.success("User status updated", appUserService.updateStatus(userId, request.status(), authentication.getName()));
    }

    @PatchMapping("/{userId}/reset-password")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> resetPassword(@PathVariable String userId, @Valid @RequestBody ResetPasswordRequest request, Authentication authentication) {
        return ApiResponse.success("Password reset", appUserService.resetPassword(userId, request.newPassword(), authentication.getName()));
    }

    /** Soft disable only - user rows are never physically deleted. */
    @DeleteMapping("/{userId}")
    @PreAuthorize("@access.allow('ADMIN')")
    public ApiResponse<UserResponse> disableUser(@PathVariable String userId, Authentication authentication) {
        return ApiResponse.success("User disabled", appUserService.disableUser(userId, authentication.getName()));
    }
}
