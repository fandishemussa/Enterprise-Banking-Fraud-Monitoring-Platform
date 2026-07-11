package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final String SYSTEM_USER = "system";

    private final AuditLogRepository auditLogRepository;

    /**
     * Runs in its own transaction so an audit entry is persisted even if the
     * caller's business transaction later rolls back for an unrelated reason.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEventType eventType, String entityType, String entityId,
                        String oldValue, String newValue, String description) {
        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .username(SYSTEM_USER)
                .role("SYSTEM")
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(AuditLogResponse::from);
    }
}
