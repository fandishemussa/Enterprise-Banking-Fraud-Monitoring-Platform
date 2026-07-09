package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String transactionId,
        String sourceAccountId,
        String destinationAccountId,
        String customerId,
        BigDecimal amount,
        String currency,
        TransactionType transactionType,
        TransactionChannel channel,
        String merchantCategory,
        String country,
        String deviceId,
        String ipAddress,
        TransactionStatus status,
        LocalDateTime createdAt
) {

    public static TransactionResponse from(BankingTransaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getSourceAccount().getAccountId(),
                transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountId() : null,
                transaction.getCustomer().getCustomerId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType(),
                transaction.getChannel(),
                transaction.getMerchantCategory(),
                transaction.getCountry(),
                transaction.getDeviceId(),
                transaction.getIpAddress(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}
