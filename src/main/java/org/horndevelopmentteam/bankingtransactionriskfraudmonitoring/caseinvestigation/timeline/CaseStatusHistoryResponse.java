package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import java.time.LocalDateTime;

public record CaseStatusHistoryResponse(
        String caseId,
        String oldStatus,
        String newStatus,
        String changedBy,
        String reason,
        LocalDateTime createdAt
) {
    public static CaseStatusHistoryResponse from(CaseStatusHistory history) {
        return new CaseStatusHistoryResponse(
                history.getCaseId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedBy(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
