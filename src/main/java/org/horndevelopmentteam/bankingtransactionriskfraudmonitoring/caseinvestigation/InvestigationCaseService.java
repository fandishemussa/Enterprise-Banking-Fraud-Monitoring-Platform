package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlert;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvestigationCaseService {

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

    @Transactional
    public CaseResponse createCase(CreateCaseRequest request) {
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

        return CaseResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getAllCases() {
        return investigationCaseRepository.findAll().stream()
                .map(CaseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseByPublicId(String caseId) {
        return CaseResponse.from(findByPublicIdOrThrow(caseId));
    }

    @Transactional
    public CaseResponse updateCase(String caseId, UpdateCaseRequest request) {
        InvestigationCase investigationCase = findByPublicIdOrThrow(caseId);
        CaseStatus oldStatus = investigationCase.getStatus();

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

        return CaseResponse.from(saved);
    }

    @Transactional
    public CaseResponse updateDecision(String caseId, CaseDecision decision) {
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

        return CaseResponse.from(saved);
    }

    public InvestigationCase findByPublicIdOrThrow(String caseId) {
        return investigationCaseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation case not found: " + caseId));
    }
}
