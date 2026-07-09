package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.testtransaction;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScoringService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Pure orchestration layer for the dashboard's "test this transaction" workflow - reuses
 * TransactionService/RiskScoringService/FraudAlertService as-is (no scoring logic is duplicated)
 * and simply composes their results into one rich response for immediate feedback in the UI.
 */
@Service
@RequiredArgsConstructor
public class TestTransactionService {

    private final TransactionService transactionService;
    private final RiskScoringService riskScoringService;
    private final FraudAlertService fraudAlertService;
    private final AuditLogService auditLogService;

    @Transactional
    public TestTransactionResponse submit(TestTransactionRequest request) {
        TransactionRequest transactionRequest = new TransactionRequest(
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency(),
                request.transactionType(),
                request.channel(),
                request.merchantCategory(),
                request.country(),
                request.deviceId(),
                request.ipAddress()
        );

        // TransactionService already runs rule scoring, calls the ML service, saves the
        // RiskScore, and generates a FraudAlert when warranted - we just read the results back.
        TransactionResponse transaction = transactionService.createTransaction(transactionRequest);

        auditLogService.record(
                AuditEventType.TEST_TRANSACTION_CREATED,
                "BankingTransaction",
                transaction.transactionId(),
                null,
                null,
                "Dashboard test transaction " + transaction.transactionId() + " created"
        );

        RiskScoreResponse riskScore = riskScoringService.getRiskScoreForTransaction(transaction.transactionId());

        auditLogService.record(
                AuditEventType.TEST_TRANSACTION_RISK_SCORED,
                "BankingTransaction",
                transaction.transactionId(),
                null,
                riskScore.riskLevel() + " (" + riskScore.finalScore() + ")",
                "Test transaction " + transaction.transactionId() + " scored " + riskScore.riskLevel()
        );

        Optional<FraudAlertResponse> fraudAlert = fraudAlertService.findByTransactionId(transaction.transactionId());
        boolean alertGenerated = fraudAlert.isPresent();

        if (alertGenerated) {
            auditLogService.record(
                    AuditEventType.TEST_TRANSACTION_ALERT_GENERATED,
                    "BankingTransaction",
                    transaction.transactionId(),
                    null,
                    fraudAlert.get().alertId(),
                    "Test transaction " + transaction.transactionId() + " generated fraud alert " + fraudAlert.get().alertId()
            );
        }

        String message = alertGenerated
                ? "Transaction scored " + riskScore.riskLevel() + " risk - fraud alert " + fraudAlert.get().alertId() + " generated"
                : "Transaction scored " + riskScore.riskLevel() + " risk - no fraud alert generated";

        return new TestTransactionResponse(transaction, riskScore, fraudAlert.orElse(null), alertGenerated, message);
    }
}
