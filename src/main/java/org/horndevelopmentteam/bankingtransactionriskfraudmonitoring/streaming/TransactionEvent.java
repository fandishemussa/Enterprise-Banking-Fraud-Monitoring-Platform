package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The wire format published to raw-transactions and consumed by TransactionEventConsumer.
 * Deliberately close to TransactionRequest's shape - customerId/status are extra context carried
 * for logging/dead-letter purposes since the actual transaction creation path derives the customer
 * from sourceAccountId and always sets status itself. */
public record TransactionEvent(
        String eventId,
        String transactionId,
        String customerId,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency,
        String transactionType,
        String channel,
        String merchantCategory,
        String country,
        String deviceId,
        String ipAddress,
        String status,
        LocalDateTime createdAt
) {
}
