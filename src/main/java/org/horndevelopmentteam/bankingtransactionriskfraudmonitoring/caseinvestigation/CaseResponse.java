package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import java.time.LocalDateTime;

public record CaseResponse(
        String caseId,
        String alertId,
        String customerId,
        String assignedTo,
        CaseStatus status,
        CasePriority priority,
        CaseDecision decision,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {

    public static CaseResponse from(InvestigationCase investigationCase) {
        return new CaseResponse(
                investigationCase.getCaseId(),
                investigationCase.getAlert().getAlertId(),
                investigationCase.getCustomer().getCustomerId(),
                investigationCase.getAssignedTo(),
                investigationCase.getStatus(),
                investigationCase.getPriority(),
                investigationCase.getDecision(),
                investigationCase.getNotes(),
                investigationCase.getCreatedAt(),
                investigationCase.getUpdatedAt(),
                investigationCase.getClosedAt()
        );
    }
}
