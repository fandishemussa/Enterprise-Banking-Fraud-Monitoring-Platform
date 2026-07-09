package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "newPassword is required") @Size(min = 8, message = "newPassword must be at least 8 characters") String newPassword
) {
}
