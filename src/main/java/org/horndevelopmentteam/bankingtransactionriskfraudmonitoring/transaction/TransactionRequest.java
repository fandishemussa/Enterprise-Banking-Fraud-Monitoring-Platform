package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "sourceAccountId is required") String sourceAccountId,
        String destinationAccountId,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be greater than 0") BigDecimal amount,
        @NotBlank(message = "currency is required") String currency,
        @NotNull(message = "transactionType is required") TransactionType transactionType,
        @NotNull(message = "channel is required") TransactionChannel channel,
        String merchantCategory,
        @NotBlank(message = "country is required") String country,
        String deviceId,
        String ipAddress
) {
}
