package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PublishTransactionEventRequest(
        @NotBlank String sourceAccountId,
        String destinationAccountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String transactionType,
        @NotBlank String channel,
        String merchantCategory,
        @NotBlank String country,
        String deviceId,
        String ipAddress
) {
}
