package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AppUser user = appUserService.authenticate(request.username(), request.password());
        String token = jwtService.generateToken(user);
        return ApiResponse.success("Login successful", new LoginResponse(token, UserResponse.from(user)));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        AppUser user = appUserService.findByUsernameOrThrow(authentication.getName());
        return ApiResponse.success(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication) {
        // Bumps tokenVersion so this (and every other outstanding) token for the user is rejected
        // by JwtAuthenticationFilter on its very next use - real revocation, not just client-side
        // token discard.
        appUserService.invalidateAllSessions(authentication.getName());
        return ApiResponse.success("Logged out", null);
    }

    @PatchMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        appUserService.changeOwnPassword(authentication.getName(), request.currentPassword(), request.newPassword());
        return ApiResponse.success("Password changed", null);
    }
}
