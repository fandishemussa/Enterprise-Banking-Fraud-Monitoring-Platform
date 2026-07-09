package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        String fullName,
        @Email(message = "email must be valid") String email,
        Role role
) {
}
