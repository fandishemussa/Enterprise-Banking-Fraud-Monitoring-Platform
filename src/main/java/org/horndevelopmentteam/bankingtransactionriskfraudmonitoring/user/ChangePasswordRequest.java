package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "currentPassword is required") String currentPassword,
        @NotBlank(message = "newPassword is required") @StrongPassword String newPassword
) {
}
