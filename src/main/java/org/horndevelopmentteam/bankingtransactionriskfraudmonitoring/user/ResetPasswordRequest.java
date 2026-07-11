package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "newPassword is required") @StrongPassword String newPassword
) {
}
