package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlert;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline.CaseTimelineEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline.CaseTimelineService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestigationCaseService {

    private static final String SYSTEM_USER = "system";
    private static final String SYSTEM_ROLE = "SYSTEM";

    private static final Map<AlertPriority, CasePriority> PRIORITY_MAPPING = Map.of(
            AlertPriority.LOW, CasePriority.LOW,
            AlertPriority.MEDIUM, CasePriority.MEDIUM,
            AlertPriority.HIGH, CasePriority.HIGH,
            AlertPriority.CRITICAL, CasePriority.CRITICAL
    );

    private final InvestigationCaseRepository investigationCaseRepository;
    private final FraudAlertService fraudAlertService;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;
    private final CaseTimelineService caseTimelineService;

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request) {
        return createCase(request, SYSTEM_USER, SYSTEM_ROLE);
    }

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request, String actorUsername, String actorRole) {
        FraudAlert alert = fraudAlertService.findByPublicIdOrThrow(request.alertId());
        LocalDateTime now = LocalDateTime.now();

        InvestigationCase investigationCase = InvestigationCase.builder()
                .caseId(idSequenceService.next("CASE"))
                .alert(alert)
                .customer(alert.getCustomer())
                .assignedTo(request.assignedTo())
                .status(CaseStatus.OPEN)
                .priority(PRIORITY_MAPPING.get(alert.getPriority()))
                .decision(CaseDecision.PENDING)
                .notes(request.notes())
                .createdAt(now)
                .updatedAt(now)
                .build();
        InvestigationCase saved = investigationCaseRepository.save(investigationCase);

        auditLogService.record(
                AuditEventType.CASE_CREATED,
                "InvestigationCase",
                saved.getCaseId(),
                null,
                saved.getStatus().name(),
                "Case " + saved.getCaseId() + " created from alert " + alert.getAlertId()
        );
        caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_CREATED,
                "Case created", "Created from alert " + alert.getAlertId(), actorUsername, actorRole);
        if (saved.getAssignedTo() != null && !saved.getAssignedTo().isBlank()) {
            caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_ASSIGNED,
                    "Case assigned", "Assigned to " + saved.getAssignedTo(), actorUsername, actorRole);
        }

        return CaseResponse.from(saved);
    }

    /** Called when an alert's status moves to INVESTIGATING (see FraudAlertController#updateStatus) so
     * the "one case per alert" investigation workflow actually gets a case without requiring a
     * separate manual step. A no-op if a case already exists for this alert - re-opening an alert
     * that was previously investigated should not spawn a duplicate case. The case inherits the
     * alert's current assignee (not null) so it actually shows up in that user's case queue -
     * getCasesVisibleTo scopes ANALYST/INVESTIGATOR/TESTER to cases assigned to them, so a case
     * with no assignee would otherwise be invisible to the very user working the alert. */
    @Transactional
    public Optional<CaseResponse> ensureCaseForAlert(String alertId, String actorUsername, String actorRole) {
        if (investigationCaseRepository.existsByAlert_AlertId(alertId)) {
            return Optional.empty();
        }
        FraudAlert alert = fraudAlertService.findByPublicIdOrThrow(alertId);
        CreateCaseRequest request = new CreateCaseRequest(alertId, alert.getAssignedTo(),
                "Auto-created: alert moved to Investigating");
        return Optional.of(createCase(request, actorUsername, actorRole));
    }

    /** Keeps a case's assignee in sync with its alert's assignee (see FraudAlertController#assignAlert
     * and #bulkAssign) - without this, reassigning the alert to someone else would leave the case
     * invisible to the new assignee (same underlying issue ensureCaseForAlert fixes for creation).
     * A no-op if no case exists yet for this alert or the assignee is already correct. */
    @Transactional
    public void syncAssigneeForAlert(String alertId, String assignedTo, String actorUsername, String actorRole) {
        investigationCaseRepository.findByAlert_AlertId(alertId).ifPresent(investigationCase -> {
            if (Objects.equals(investigationCase.getAssignedTo(), assignedTo)) {
                return;
            }
            investigationCase.setAssignedTo(assignedTo);
            investigationCase.setUpdatedAt(LocalDateTime.now());
            investigationCaseRepository.save(investigationCase);
            caseTimelineService.recordEvent(investigationCase.getCaseId(), CaseTimelineEventType.CASE_ASSIGNED,
                    "Case assigned", "Assigned to " + assignedTo + " (following alert assignment)",
                    actorUsername, actorRole);
        });
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getAllCases() {
        return investigationCaseRepository.findAll().stream()
                .map(CaseResponse::from)
                .toList();
    }

    /** Same visibility rule as alerts (see FraudAlertService#getAlertsVisibleTo): only the roles that
     * actually work cases (ANALYST/INVESTIGATOR/TESTER) get scoped to cases assigned to them; ADMIN,
     * VIEWER, and anything unrecognized (e.g. the anonymous principal when SECURITY_ENABLED=false)
     * see everything. */
    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesVisibleTo(String username, String role) {
        boolean scoped = "ANALYST".equals(role) || "INVESTIGATOR".equals(role) || "TESTER".equals(role);
        if (!scoped) {
            return getAllCases();
        }
        return investigationCaseRepository.findByAssignedTo(username).stream()
                .map(CaseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseByPublicId(String caseId) {
        return CaseResponse.from(findByPublicIdOrThrow(caseId));
    }

    @Transactional
    public CaseResponse updateCase(String caseId, UpdateCaseRequest request) {
        return updateCase(caseId, request, SYSTEM_USER, SYSTEM_ROLE);
    }

    @Transactional
    public CaseResponse updateCase(String caseId, UpdateCaseRequest request, String actorUsername, String actorRole) {
        InvestigationCase investigationCase = findByPublicIdOrThrow(caseId);
        CaseStatus oldStatus = investigationCase.getStatus();
        String oldAssignedTo = investigationCase.getAssignedTo();

        if (request.status() != null) {
            investigationCase.setStatus(request.status());
            if (request.status() == CaseStatus.RESOLVED || request.status() == CaseStatus.CLOSED) {
                investigationCase.setClosedAt(LocalDateTime.now());
            }
        }
        if (request.assignedTo() != null) {
            investigationCase.setAssignedTo(request.assignedTo());
        }
        if (request.notes() != null) {
            investigationCase.setNotes(request.notes());
        }
        investigationCase.setUpdatedAt(LocalDateTime.now());
        InvestigationCase saved = investigationCaseRepository.save(investigationCase);

        auditLogService.record(
                AuditEventType.CASE_UPDATED,
                "InvestigationCase",
                saved.getCaseId(),
                oldStatus.name(),
                saved.getStatus().name(),
                "Case " + saved.getCaseId() + " updated"
        );

        if (request.status() != null && request.status() != oldStatus) {
            caseTimelineService.recordStatusChange(saved.getCaseId(), oldStatus.name(), saved.getStatus().name(),
                    actorUsername, "Status changed via case update");
            caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.STATUS_CHANGED,
                    "Status changed", oldStatus + " -> " + saved.getStatus(), actorUsername, actorRole);
            if (saved.getStatus() == CaseStatus.ESCALATED) {
                caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_ESCALATED,
                        "Case escalated", null, actorUsername, actorRole);
            } else if (saved.getStatus() == CaseStatus.RESOLVED) {
                caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_RESOLVED,
                        "Case resolved", null, actorUsername, actorRole);
            } else if (saved.getStatus() == CaseStatus.CLOSED) {
                caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_CLOSED,
                        "Case closed", null, actorUsername, actorRole);
            }
        }
        if (request.assignedTo() != null && !Objects.equals(request.assignedTo(), oldAssignedTo)) {
            caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.CASE_ASSIGNED,
                    "Case assigned", "Assigned to " + request.assignedTo(), actorUsername, actorRole);
        }

        return CaseResponse.from(saved);
    }

    @Transactional
    public CaseResponse updateDecision(String caseId, CaseDecision decision) {
        return updateDecision(caseId, decision, SYSTEM_USER, SYSTEM_ROLE);
    }

    @Transactional
    public CaseResponse updateDecision(String caseId, CaseDecision decision, String actorUsername, String actorRole) {
        InvestigationCase investigationCase = findByPublicIdOrThrow(caseId);
        CaseDecision oldDecision = investigationCase.getDecision();
        investigationCase.setDecision(decision);
        investigationCase.setUpdatedAt(LocalDateTime.now());
        InvestigationCase saved = investigationCaseRepository.save(investigationCase);

        auditLogService.record(
                AuditEventType.CASE_DECISION_UPDATED,
                "InvestigationCase",
                saved.getCaseId(),
                oldDecision.name(),
                decision.name(),
                "Case " + saved.getCaseId() + " decision changed from " + oldDecision + " to " + decision
        );
        caseTimelineService.recordEvent(saved.getCaseId(), CaseTimelineEventType.DECISION_UPDATED,
                "Decision updated", oldDecision + " -> " + decision, actorUsername, actorRole);

        return CaseResponse.from(saved);
    }

    public InvestigationCase findByPublicIdOrThrow(String caseId) {
        return investigationCaseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation case not found: " + caseId));
    }
}
