package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        AuditEventType eventType,
        String username,
        String role,
        String entityType,
        String entityId,
        String oldValue,
        String newValue,
        String description,
        LocalDateTime createdAt
) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getEventType(),
                log.getUsername(),
                log.getRole(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getDescription(),
                log.getCreatedAt()
        );
    }
}
