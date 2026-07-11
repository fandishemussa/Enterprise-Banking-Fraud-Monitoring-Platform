package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bridges the streaming pipeline to the existing REST transaction-creation path - deliberately
 * does NOT re-implement rule scoring, ML scoring, or alert generation. TransactionEventConsumer
 * hands a raw event here; this converts it to a TransactionRequest and calls
 * TransactionService.createTransaction(...), the same method POST /api/v1/transactions uses, so
 * both entry points always score and alert identically.
 */
@Service
@RequiredArgsConstructor
public class StreamingTransactionService {

    private static final Logger log = LoggerFactory.getLogger(StreamingTransactionService.class);

    private final TransactionEventValidator transactionEventValidator;
    private final TransactionService transactionService;
    private final FraudAlertService fraudAlertService;
    private final TransactionEventProducer transactionEventProducer;
    private final DeadLetterEventPublisher deadLetterEventPublisher;
    private final StreamingMetricsService streamingMetricsService;
    private final StreamingEventLogRepository streamingEventLogRepository;
    private final AuditLogService auditLogService;
    private final IdSequenceService idSequenceService;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Transactional
    public void processRawEvent(TransactionEvent event, String rawPayload, String sourceTopic) {
        LocalDateTime start = LocalDateTime.now();
        List<String> validationErrors = transactionEventValidator.validate(event);
        if (!validationErrors.isEmpty()) {
            String reason = String.join("; ", validationErrors);
            log.warn("Rejecting invalid streaming transaction event {}: {}", event != null ? event.eventId() : "unknown", reason);
            deadLetterEventPublisher.send(
                    event != null && event.eventId() != null ? event.eventId() : idSequenceService.next("EVT"),
                    sourceTopic, rawPayload, "VALIDATION_ERROR", reason);
            logEvent(event, sourceTopic, "FAILED", reason);
            return;
        }

        try {
            TransactionRequest request = new TransactionRequest(
                    event.sourceAccountId(),
                    event.destinationAccountId(),
                    event.amount(),
                    event.currency(),
                    TransactionType.valueOf(event.transactionType()),
                    TransactionChannel.valueOf(event.channel()),
                    event.merchantCategory(),
                    event.country(),
                    event.deviceId(),
                    event.ipAddress()
            );

            TransactionResponse response = transactionService.createTransaction(request);
            boolean alertGenerated = fraudAlertService.findByTransactionId(response.transactionId()).isPresent();

            long latencyMs = Duration.between(start, LocalDateTime.now()).toMillis();
            streamingMetricsService.recordConsumed(latencyMs, alertGenerated);
            logEvent(event, sourceTopic, "SUCCESS", "Processed as transaction " + response.transactionId());

            auditLogService.record(AuditEventType.STREAMING_EVENT_CONSUMED, "TransactionEvent", event.eventId(),
                    null, response.transactionId(),
                    "Streaming event " + event.eventId() + " processed as transaction " + response.transactionId());

            transactionEventProducer.publish(kafkaTopicsProperties.getScoredTransactions(), response.transactionId(), response);
            if (alertGenerated) {
                transactionEventProducer.publish(kafkaTopicsProperties.getFraudAlerts(), response.transactionId(),
                        fraudAlertService.findByTransactionId(response.transactionId()).orElse(null));
            }
        } catch (StreamingUnavailableException ex) {
            // Transaction + scoring already succeeded and committed - a downstream publish
            // failure (scored-transactions/fraud-alerts) shouldn't be treated as event processing
            // failure or sent to dead-letter, since re-processing would create a duplicate transaction.
            log.warn("Streaming event {} processed successfully but downstream publish failed: {}",
                    event.eventId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to process streaming transaction event {}: {}", event.eventId(), ex.getMessage());
            deadLetterEventPublisher.send(event.eventId(), sourceTopic, rawPayload,
                    ex.getClass().getSimpleName(), ex.getMessage());
            logEvent(event, sourceTopic, "FAILED", ex.getMessage());
        }
    }

    private void logEvent(TransactionEvent event, String topic, String status, String message) {
        streamingEventLogRepository.save(StreamingEventLog.builder()
                .eventId(event != null && event.eventId() != null ? event.eventId() : "unknown")
                .topic(topic)
                .eventType("RAW_TRANSACTION")
                .transactionId(event != null ? event.transactionId() : null)
                .status(status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
