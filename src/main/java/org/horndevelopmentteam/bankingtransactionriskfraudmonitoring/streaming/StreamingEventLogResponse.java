package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import java.time.LocalDateTime;

public record StreamingEventLogResponse(
        String eventId,
        String topic,
        String eventType,
        String transactionId,
        String status,
        String message,
        LocalDateTime createdAt
) {
    public static StreamingEventLogResponse from(StreamingEventLog log) {
        return new StreamingEventLogResponse(
                log.getEventId(), log.getTopic(), log.getEventType(), log.getTransactionId(),
                log.getStatus(), log.getMessage(), log.getCreatedAt()
        );
    }
}
