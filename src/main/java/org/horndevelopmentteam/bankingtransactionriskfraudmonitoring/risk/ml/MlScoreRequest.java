package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml;

import java.math.BigDecimal;

/** Mirrors ml-service's TransactionScoreRequest schema field-for-field (camelCase). */
public record MlScoreRequest(
        String transactionId,
        BigDecimal amount,
        String transactionType,
        int hourOfDay,
        int transactionFrequency,
        int failedAttemptCount,
        int countryRiskScore,
        boolean newDevice,
        boolean newCountry,
        int merchantRiskScore,
        double customerAverageAmountRatio,
        BigDecimal sourceOldBalance,
        BigDecimal sourceNewBalance,
        BigDecimal destinationOldBalance,
        BigDecimal destinationNewBalance,
        boolean flaggedFraud
) {
}
