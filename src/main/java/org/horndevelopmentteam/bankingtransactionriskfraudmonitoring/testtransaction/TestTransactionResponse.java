package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.testtransaction;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;

public record TestTransactionResponse(
        TransactionResponse transaction,
        RiskScoreResponse riskScore,
        FraudAlertResponse fraudAlert,
        boolean alertGenerated,
        String message
) {
}
