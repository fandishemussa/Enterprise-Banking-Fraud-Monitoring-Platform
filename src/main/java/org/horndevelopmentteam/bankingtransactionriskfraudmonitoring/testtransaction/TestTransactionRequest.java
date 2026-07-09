package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.testtransaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;

import java.math.BigDecimal;

/**
 * customerId and status are accepted for dashboard-form convenience but are not independently
 * applied - the real customer is always derived from sourceAccountId (same as the production
 * transaction flow) and status is always SUCCESS, exactly matching POST /api/v1/transactions.
 */
public record TestTransactionRequest(
        String customerId,
        @NotBlank(message = "sourceAccountId is required") String sourceAccountId,
        String destinationAccountId,
        @NotNull(message = "amount is required") @DecimalMin(value = "0.01", message = "amount must be greater than 0") BigDecimal amount,
        @NotBlank(message = "currency is required") String currency,
        @NotNull(message = "transactionType is required") TransactionType transactionType,
        @NotNull(message = "channel is required") TransactionChannel channel,
        String merchantCategory,
        @NotBlank(message = "country is required") String country,
        String deviceId,
        String ipAddress,
        String status
) {
}
