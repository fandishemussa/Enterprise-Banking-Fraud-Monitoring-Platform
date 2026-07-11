package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import java.time.LocalDateTime;

public record DeadLetterEventResponse(
        String eventId,
        String sourceTopic,
        String rawPayload,
        String errorReason,
        String errorType,
        DeadLetterEventStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DeadLetterEventResponse from(DeadLetterEvent event) {
        return new DeadLetterEventResponse(
                event.getEventId(), event.getSourceTopic(), event.getRawPayload(), event.getErrorReason(),
                event.getErrorType(), event.getStatus(), event.getCreatedAt(), event.getUpdatedAt()
        );
    }
}
