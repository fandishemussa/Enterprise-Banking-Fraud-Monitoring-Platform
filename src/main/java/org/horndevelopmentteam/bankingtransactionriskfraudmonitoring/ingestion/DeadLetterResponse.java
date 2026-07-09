package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import java.time.LocalDateTime;

public record DeadLetterResponse(
        String eventId,
        String rawPayload,
        String errorType,
        String errorReason,
        DeadLetterStatus processedStatus,
        LocalDateTime receivedAt,
        LocalDateTime lastRetryAt
) {

    public static DeadLetterResponse from(DeadLetterTransaction entity) {
        return new DeadLetterResponse(
                entity.getEventId(),
                entity.getRawPayload(),
                entity.getErrorType(),
                entity.getErrorReason(),
                entity.getProcessedStatus(),
                entity.getReceivedAt(),
                entity.getLastRetryAt()
        );
    }
}
