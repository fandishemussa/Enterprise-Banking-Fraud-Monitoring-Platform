package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "fullName is required") String fullName,
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        String phone,
        @NotBlank(message = "country is required") String country
) {
}
