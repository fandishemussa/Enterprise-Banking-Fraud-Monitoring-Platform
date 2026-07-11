package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import java.time.LocalDateTime;

public record AlertEscalationResponse(
        String escalationId,
        String alertId,
        String escalatedFrom,
        String escalatedTo,
        String reason,
        int escalationLevel,
        LocalDateTime createdAt
) {
    public static AlertEscalationResponse from(AlertEscalation escalation) {
        return new AlertEscalationResponse(
                escalation.getEscalationId(),
                escalation.getAlertId(),
                escalation.getEscalatedFrom(),
                escalation.getEscalatedTo(),
                escalation.getReason(),
                escalation.getEscalationLevel(),
                escalation.getCreatedAt()
        );
    }
}
