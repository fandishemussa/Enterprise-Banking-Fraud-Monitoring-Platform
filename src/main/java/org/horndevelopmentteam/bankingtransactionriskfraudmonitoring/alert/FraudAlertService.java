package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalationRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertEscalationResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertSlaService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FraudAlertService {

    private static final String SYSTEM_USER = "system";

    private final FraudAlertRepository fraudAlertRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;
    private final AlertSlaService alertSlaService;

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

        meterRegistry.counter("fraud_alerts_created_total", "priority", priority.name()).increment();

        alertSlaService.createSlaResult(saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAllAlerts(Pageable pageable) {
        return fraudAlertRepository.findAll(pageable).map(FraudAlertResponse::from);
    }

    /** Only the roles that actually work the alert queue (ANALYST/INVESTIGATOR/TESTER) get scoped
     * to alerts assigned to them; everyone else sees everything - ADMIN and VIEWER for oversight,
     * and anything else (including the anonymous principal when SECURITY_ENABLED=false) defaults
     * to unrestricted rather than silently filtering to an empty list. */
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAlertsVisibleTo(String username, String role, Pageable pageable) {
        boolean scoped = "ANALYST".equals(role) || "INVESTIGATOR".equals(role) || "TESTER".equals(role);
        if (!scoped) {
            return getAllAlerts(pageable);
        }
        return fraudAlertRepository.findByAssignedTo(username, pageable).map(FraudAlertResponse::from);
    }

    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertByPublicId(String alertId) {
        return FraudAlertResponse.from(findByPublicIdOrThrow(alertId));
    }

    @Transactional
    public FraudAlertResponse updateStatus(String alertId, AlertStatus newStatus) {
        return updateStatus(alertId, newStatus, SYSTEM_USER);
    }

    @Transactional
    public FraudAlertResponse updateStatus(String alertId, AlertStatus newStatus, String actorUsername) {
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

        alertSlaService.recordStatusChange(saved, oldStatus, newStatus, actorUsername,
                "Status changed via alert update");

        return FraudAlertResponse.from(saved);
    }

    @Transactional
    public FraudAlertResponse assign(String alertId, String assignedTo) {
        FraudAlert alert = findByPublicIdOrThrow(alertId);
        alert.setAssignedTo(assignedTo);
        alert.setUpdatedAt(LocalDateTime.now());
        return FraudAlertResponse.from(fraudAlertRepository.save(alert));
    }

    @Transactional
    public AlertEscalationResponse escalate(String alertId, AlertEscalationRequest request, String actorUsername) {
        FraudAlert alert = findByPublicIdOrThrow(alertId);
        AlertStatus oldStatus = alert.getStatus();
        String previousAssignee = alert.getAssignedTo();

        alert.setStatus(AlertStatus.ESCALATED);
        alert.setAssignedTo(request.escalatedTo());
        alert.setUpdatedAt(LocalDateTime.now());
        FraudAlert saved = fraudAlertRepository.save(alert);

        auditLogService.record(
                AuditEventType.ALERT_STATUS_UPDATED,
                "FraudAlert",
                saved.getAlertId(),
                oldStatus.name(),
                AlertStatus.ESCALATED.name(),
                "Alert " + saved.getAlertId() + " escalated from " + oldStatus + " by " + actorUsername
        );
        alertSlaService.recordStatusChange(saved, oldStatus, AlertStatus.ESCALATED, actorUsername,
                request.reason() != null ? request.reason() : "Alert escalated");

        return alertSlaService.escalate(alertId, previousAssignee, request, actorUsername);
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
