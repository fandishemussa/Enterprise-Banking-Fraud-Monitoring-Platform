package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "fullName is required") String fullName,
        @NotBlank(message = "password is required") @StrongPassword String password,
        @NotNull(message = "role is required") Role role
) {
}
