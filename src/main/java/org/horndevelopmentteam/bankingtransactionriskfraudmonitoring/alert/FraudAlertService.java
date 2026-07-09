package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private final FraudAlertRepository fraudAlertRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    /**
     * Only HIGH/CRITICAL risk transactions reach this method (see TransactionService);
     * returns empty when the risk level does not warrant an alert.
     */
    @Transactional
    public FraudAlert createAlertIfWarranted(BankingTransaction transaction, RiskScore riskScore) {
        if (riskScore.getRiskLevel() != RiskLevel.HIGH && riskScore.getRiskLevel() != RiskLevel.CRITICAL) {
            return null;
        }

        AlertPriority priority = riskScore.getRiskLevel() == RiskLevel.CRITICAL
                ? AlertPriority.CRITICAL
                : AlertPriority.HIGH;

        LocalDateTime now = LocalDateTime.now();
        FraudAlert alert = FraudAlert.builder()
                .alertId(idSequenceService.next("ALERT"))
                .transaction(transaction)
                .customer(transaction.getCustomer())
                .riskScore(riskScore)
                .alertType(AlertType.RULE_BASED)
                .priority(priority)
                .message("Transaction " + transaction.getTransactionId() + " flagged as " + riskScore.getRiskLevel()
                        + " risk (score " + riskScore.getFinalScore() + "): " + riskScore.getExplanation())
                .status(AlertStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();
        FraudAlert saved = fraudAlertRepository.save(alert);

        auditLogService.record(
                AuditEventType.FRAUD_ALERT_CREATED,
                "FraudAlert",
                saved.getAlertId(),
                null,
                saved.getStatus().name(),
                "Fraud alert " + saved.getAlertId() + " created for transaction " + transaction.getTransactionId()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAllAlerts() {
        return fraudAlertRepository.findAll().stream()
                .map(FraudAlertResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertByPublicId(String alertId) {
        return FraudAlertResponse.from(findByPublicIdOrThrow(alertId));
    }

    @Transactional
    public FraudAlertResponse updateStatus(String alertId, AlertStatus newStatus) {
        FraudAlert alert = findByPublicIdOrThrow(alertId);
        AlertStatus oldStatus = alert.getStatus();
        alert.setStatus(newStatus);
        alert.setUpdatedAt(LocalDateTime.now());
        if (newStatus == AlertStatus.RESOLVED || newStatus == AlertStatus.FALSE_POSITIVE
                || newStatus == AlertStatus.CONFIRMED_FRAUD) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        FraudAlert saved = fraudAlertRepository.save(alert);

        auditLogService.record(
                AuditEventType.ALERT_STATUS_UPDATED,
                "FraudAlert",
                saved.getAlertId(),
                oldStatus.name(),
                newStatus.name(),
                "Alert " + saved.getAlertId() + " status changed from " + oldStatus + " to " + newStatus
        );

        return FraudAlertResponse.from(saved);
    }

    @Transactional
    public FraudAlertResponse assign(String alertId, String assignedTo) {
        FraudAlert alert = findByPublicIdOrThrow(alertId);
        alert.setAssignedTo(assignedTo);
        alert.setUpdatedAt(LocalDateTime.now());
        return FraudAlertResponse.from(fraudAlertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> getAlertsForCustomer(Customer customer) {
        return fraudAlertRepository.findByCustomer(customer).stream()
                .map(FraudAlertResponse::from)
                .toList();
    }

    public FraudAlert findByPublicIdOrThrow(String alertId) {
        return fraudAlertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found: " + alertId));
    }

    @Transactional(readOnly = true)
    public Optional<FraudAlertResponse> findByTransactionId(String transactionId) {
        return fraudAlertRepository.findByTransaction_TransactionId(transactionId).map(FraudAlertResponse::from);
    }
}
