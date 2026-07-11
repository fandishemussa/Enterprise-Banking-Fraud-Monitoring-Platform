package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeadLetterEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterEventPublisher.class);

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final TransactionEventProducer transactionEventProducer;
    private final StreamingMetricsService streamingMetricsService;
    private final AuditLogService auditLogService;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Transactional
    public void send(String eventId, String sourceTopic, String rawPayload, String errorType, String errorReason) {
        LocalDateTime now = LocalDateTime.now();
        DeadLetterEvent event = DeadLetterEvent.builder()
                .eventId(eventId)
                .sourceTopic(sourceTopic)
                .rawPayload(rawPayload)
                .errorType(errorType)
                .errorReason(errorReason)
                .status(DeadLetterEventStatus.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();
        deadLetterEventRepository.save(event);
        streamingMetricsService.recordFailed();

        auditLogService.record(AuditEventType.STREAMING_EVENT_DEAD_LETTERED, "TransactionEvent", eventId,
                null, errorType, "Event " + eventId + " from topic " + sourceTopic + " dead-lettered: " + errorReason);

        try {
            transactionEventProducer.publish(kafkaTopicsProperties.getDeadLetterTransactions(), eventId, event);
        } catch (StreamingUnavailableException ex) {
            log.warn("Could not publish dead-letter event {} to Kafka (still recorded in the database): {}",
                    eventId, ex.getMessage());
        }
    }
}
