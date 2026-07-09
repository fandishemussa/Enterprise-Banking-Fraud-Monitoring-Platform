package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml;

import java.util.List;

/** Mirrors ml-service's TransactionScoreResponse schema field-for-field (camelCase). */
public record MlScoreResponse(
        String transactionId,
        int mlScore,
        boolean isAnomaly,
        String riskLevel,
        List<String> explanation,
        String modelVersion,
        boolean fallbackMode
) {
}
