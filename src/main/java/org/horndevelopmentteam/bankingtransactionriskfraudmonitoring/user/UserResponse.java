package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import java.time.LocalDateTime;

/** Never includes passwordHash. */
public record UserResponse(
        String userId,
        String username,
        String email,
        String fullName,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        int failedLoginAttempts,
        LocalDateTime lockedUntil
) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getFailedLoginAttempts(),
                user.getLockedUntil()
        );
    }
}
