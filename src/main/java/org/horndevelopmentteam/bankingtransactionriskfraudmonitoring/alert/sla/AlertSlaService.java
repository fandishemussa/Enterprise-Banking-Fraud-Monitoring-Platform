package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlert;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Tracks alert response/resolution deadlines, escalation, and status history. SLA status
 * (ON_TRACK/NEAR_BREACH/BREACHED) isn't updated by a background job - it's recomputed against the
 * current time whenever this service is read from (getSummary/getAllResults/etc), which is simple
 * and correct at this system's polling-driven dashboard usage without needing @EnableScheduling.
 */
@Service
@RequiredArgsConstructor
public class AlertSlaService {

    private static final Logger log = LoggerFactory.getLogger(AlertSlaService.class);
    private static final Set<AlertStatus> TERMINAL_STATUSES = Set.of(
            AlertStatus.RESOLVED, AlertStatus.CONFIRMED_FRAUD, AlertStatus.FALSE_POSITIVE);
    /** Fraction of the remaining response/resolution window at which an on-track SLA flips to
     * near-breach - e.g. 0.2 means "less than 20% of the window remains". */
    private static final double NEAR_BREACH_THRESHOLD = 0.2;

    private final AlertSlaPolicyRepository alertSlaPolicyRepository;
    private final AlertSlaResultRepository alertSlaResultRepository;
    private final AlertEscalationRepository alertEscalationRepository;
    private final AlertStatusHistoryRepository alertStatusHistoryRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public void createSlaResult(FraudAlert alert) {
        AlertSlaPolicy policy = alertSlaPolicyRepository.findByPriority(alert.getPriority()).orElse(null);
        if (policy == null || !Boolean.TRUE.equals(policy.getEnabled())) {
            log.warn("No enabled SLA policy for priority {} - alert {} will not be SLA-tracked",
                    alert.getPriority(), alert.getAlertId());
            return;
        }

        LocalDateTime now = alert.getCreatedAt();
        AlertSlaResult result = AlertSlaResult.builder()
                .alertId(alert.getAlertId())
                .policyId(policy.getPolicyId())
                .responseDeadline(now.plusMinutes(policy.getResponseTimeMinutes()))
                .resolutionDeadline(now.plusMinutes(policy.getResolutionTimeMinutes()))
                .responseBreached(false)
                .resolutionBreached(false)
                .status(SlaStatus.ON_TRACK)
                .createdAt(now)
                .updatedAt(now)
                .build();
        alertSlaResultRepository.save(result);

        auditLogService.record(AuditEventType.SLA_RESULT_CREATED, "FraudAlert", alert.getAlertId(),
                null, policy.getPolicyId(),
                "SLA tracking started for alert " + alert.getAlertId() + " under policy " + policy.getPolicyId());
    }

    @Transactional
    public void recordStatusChange(FraudAlert alert, AlertStatus oldStatus, AlertStatus newStatus,
                                    String changedBy, String reason) {
        AlertStatusHistory history = AlertStatusHistory.builder()
                .alertId(alert.getAlertId())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .changedBy(changedBy)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        alertStatusHistoryRepository.save(history);
        auditLogService.record(AuditEventType.ALERT_STATUS_HISTORY_CREATED, "FraudAlert", alert.getAlertId(),
                oldStatus.name(), newStatus.name(), "Alert " + alert.getAlertId() + " status history recorded");

        alertSlaResultRepository.findByAlertId(alert.getAlertId()).ifPresent(result -> {
            LocalDateTime now = LocalDateTime.now();
            if (newStatus == AlertStatus.ACKNOWLEDGED && result.getFirstAcknowledgedAt() == null) {
                result.setFirstAcknowledgedAt(now);
                if (now.isAfter(result.getResponseDeadline())) {
                    result.setResponseBreached(true);
                }
            }
            if (TERMINAL_STATUSES.contains(newStatus) && result.getResolvedAt() == null) {
                result.setResolvedAt(now);
                if (now.isAfter(result.getResolutionDeadline())) {
                    result.setResolutionBreached(true);
                }
                result.setStatus(SlaStatus.COMPLETED);
                if (Boolean.TRUE.equals(result.getResponseBreached()) || Boolean.TRUE.equals(result.getResolutionBreached())) {
                    auditLogService.record(AuditEventType.ALERT_SLA_BREACHED, "FraudAlert", alert.getAlertId(),
                            null, null, "Alert " + alert.getAlertId() + " completed with an SLA breach");
                }
            }
            result.setUpdatedAt(now);
            alertSlaResultRepository.save(result);
        });
    }

    @Transactional
    public AlertEscalationResponse escalate(String alertId, String escalatedFrom, AlertEscalationRequest request, String actorUsername) {
        int nextLevel = alertEscalationRepository.countByAlertId(alertId) + 1;
        AlertEscalation escalation = AlertEscalation.builder()
                .escalationId(idSequenceService.next("ESC"))
                .alertId(alertId)
                .escalatedFrom(escalatedFrom != null ? escalatedFrom : "unassigned")
                .escalatedTo(request.escalatedTo())
                .reason(request.reason())
                .escalationLevel(nextLevel)
                .createdAt(LocalDateTime.now())
                .build();
        AlertEscalation saved = alertEscalationRepository.save(escalation);

        auditLogService.record(AuditEventType.ALERT_ESCALATED, "FraudAlert", alertId,
                escalation.getEscalatedFrom(), escalation.getEscalatedTo(),
                "Alert " + alertId + " escalated (level " + nextLevel + ") to " + escalation.getEscalatedTo()
                        + " by " + actorUsername);

        return AlertEscalationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AlertEscalationResponse> getEscalations(String alertId) {
        return alertEscalationRepository.findByAlertIdOrderByCreatedAtDesc(alertId).stream()
                .map(AlertEscalationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlertStatusHistoryResponse> getStatusHistory(String alertId) {
        return alertStatusHistoryRepository.findByAlertIdOrderByCreatedAtDesc(alertId).stream()
                .map(AlertStatusHistoryResponse::from)
                .toList();
    }

    @Transactional
    public List<AlertSlaResultResponse> getAllSlaResults() {
        return refreshAndFetch(alertSlaResultRepository.findAll());
    }

    @Transactional
    public List<AlertSlaResultResponse> getBreached() {
        return refreshAndFetch(alertSlaResultRepository.findAll()).stream()
                .filter(r -> r.status() == SlaStatus.BREACHED)
                .toList();
    }

    @Transactional
    public List<AlertSlaResultResponse> getNearBreach() {
        return refreshAndFetch(alertSlaResultRepository.findAll()).stream()
                .filter(r -> r.status() == SlaStatus.NEAR_BREACH)
                .toList();
    }

    @Transactional
    public SlaSummaryResponse getSummary() {
        List<AlertSlaResultResponse> results = refreshAndFetch(alertSlaResultRepository.findAll());
        long total = results.size();
        long onTrack = results.stream().filter(r -> r.status() == SlaStatus.ON_TRACK).count();
        long nearBreach = results.stream().filter(r -> r.status() == SlaStatus.NEAR_BREACH).count();
        long breached = results.stream().filter(r -> r.status() == SlaStatus.BREACHED).count();
        long completed = results.stream().filter(r -> r.status() == SlaStatus.COMPLETED).count();
        long completedWithoutBreach = results.stream()
                .filter(r -> r.status() == SlaStatus.COMPLETED && !r.responseBreached() && !r.resolutionBreached())
                .count();
        double complianceRate = completed > 0 ? (completedWithoutBreach * 100.0) / completed : 100.0;

        double avgResponseMinutes = alertSlaResultRepository.findAll().stream()
                .filter(r -> r.getFirstAcknowledgedAt() != null)
                .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getFirstAcknowledgedAt()).toMinutes())
                .average().orElse(0);
        double avgResolutionMinutes = alertSlaResultRepository.findAll().stream()
                .filter(r -> r.getResolvedAt() != null)
                .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getResolvedAt()).toMinutes())
                .average().orElse(0);

        return new SlaSummaryResponse(total, onTrack, nearBreach, breached, completed,
                complianceRate, avgResponseMinutes, avgResolutionMinutes);
    }

    @Transactional(readOnly = true)
    public List<AlertSlaPolicyResponse> getPolicies() {
        return alertSlaPolicyRepository.findAll().stream().map(AlertSlaPolicyResponse::from).toList();
    }

    @Transactional
    public AlertSlaPolicyResponse updatePolicy(String policyId, AlertSlaPolicyRequest request, String actorUsername) {
        AlertSlaPolicy policy = alertSlaPolicyRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("SLA policy not found: " + policyId));
        policy.setResponseTimeMinutes(request.responseTimeMinutes());
        policy.setResolutionTimeMinutes(request.resolutionTimeMinutes());
        if (request.enabled() != null) {
            policy.setEnabled(request.enabled());
        }
        policy.setUpdatedAt(LocalDateTime.now());
        AlertSlaPolicy saved = alertSlaPolicyRepository.save(policy);

        auditLogService.record(AuditEventType.SLA_POLICY_UPDATED, "AlertSlaPolicy", policyId,
                null, null, "SLA policy " + policyId + " updated by " + actorUsername);

        return AlertSlaPolicyResponse.from(saved);
    }

    /** Recomputes ON_TRACK/NEAR_BREACH/BREACHED for any result not yet COMPLETED, persists any
     * status change, and returns the (now up to date) response list. */
    private List<AlertSlaResultResponse> refreshAndFetch(List<AlertSlaResult> results) {
        LocalDateTime now = LocalDateTime.now();
        for (AlertSlaResult result : results) {
            if (result.getStatus() == SlaStatus.COMPLETED) continue;

            boolean acknowledged = result.getFirstAcknowledgedAt() != null;
            LocalDateTime deadline = acknowledged ? result.getResolutionDeadline() : result.getResponseDeadline();
            LocalDateTime windowStart = acknowledged ? result.getFirstAcknowledgedAt() : result.getCreatedAt();

            SlaStatus newStatus;
            if (now.isAfter(deadline)) {
                newStatus = SlaStatus.BREACHED;
                if (!acknowledged) result.setResponseBreached(true);
                else result.setResolutionBreached(true);
            } else {
                long totalWindow = Duration.between(windowStart, deadline).toMinutes();
                long remaining = Duration.between(now, deadline).toMinutes();
                newStatus = (totalWindow > 0 && remaining <= totalWindow * NEAR_BREACH_THRESHOLD)
                        ? SlaStatus.NEAR_BREACH
                        : SlaStatus.ON_TRACK;
            }

            if (newStatus != result.getStatus()) {
                result.setStatus(newStatus);
                result.setUpdatedAt(now);
                alertSlaResultRepository.save(result);
                if (newStatus == SlaStatus.BREACHED) {
                    auditLogService.record(AuditEventType.ALERT_SLA_BREACHED, "FraudAlert", result.getAlertId(),
                            null, null, "Alert " + result.getAlertId() + " SLA breached");
                }
            }
        }
        return results.stream().map(AlertSlaResultResponse::from).toList();
    }
}
