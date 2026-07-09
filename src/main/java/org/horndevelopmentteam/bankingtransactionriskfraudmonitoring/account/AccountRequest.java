package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "customerId is required") String customerId,
        @NotNull(message = "accountType is required") AccountType accountType,
        @NotNull(message = "balance is required") @DecimalMin(value = "0", message = "balance cannot be negative") BigDecimal balance,
        @NotBlank(message = "currency is required") String currency
) {
}
