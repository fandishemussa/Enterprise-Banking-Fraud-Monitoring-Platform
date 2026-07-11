package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import java.time.LocalDateTime;

public record AlertStatusHistoryResponse(
        String alertId,
        String oldStatus,
        String newStatus,
        String changedBy,
        String reason,
        LocalDateTime createdAt
) {
    public static AlertStatusHistoryResponse from(AlertStatusHistory history) {
        return new AlertStatusHistoryResponse(
                history.getAlertId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedBy(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
