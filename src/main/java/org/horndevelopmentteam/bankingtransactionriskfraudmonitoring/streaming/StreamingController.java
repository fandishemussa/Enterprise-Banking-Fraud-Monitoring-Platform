package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/streaming")
@RequiredArgsConstructor
public class StreamingController {

    private static final int RECENT_EVENTS_LIMIT = 100;

    private final TransactionEventProducer transactionEventProducer;
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final IdSequenceService idSequenceService;
    private final StreamingMetricsService streamingMetricsService;
    private final StreamingEventLogRepository streamingEventLogRepository;
    private final DeadLetterEventRepository deadLetterEventRepository;
    private final AuditLogService auditLogService;

    @PostMapping("/transactions/publish")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST', 'TESTER')")
    public ApiResponse<String> publishTransaction(@Valid @RequestBody PublishTransactionEventRequest request) {
        String eventId = idSequenceService.next("EVT");
        TransactionEvent event = new TransactionEvent(
                eventId, null, null,
                request.sourceAccountId(), request.destinationAccountId(), request.amount(), request.currency(),
                request.transactionType(), request.channel(), request.merchantCategory(), request.country(),
                request.deviceId(), request.ipAddress(), "PENDING", LocalDateTime.now()
        );

        transactionEventProducer.publish(kafkaTopicsProperties.getRawTransactions(), eventId, event);
        streamingMetricsService.recordProduced();
        auditLogService.record(AuditEventType.STREAMING_EVENT_PUBLISHED, "TransactionEvent", eventId,
                null, null, "Streaming event " + eventId + " published to raw-transactions");

        return ApiResponse.success("Transaction event published", eventId);
    }

    @GetMapping("/metrics")
    public ApiResponse<StreamingMetricResponse> getMetrics() {
        return ApiResponse.success(StreamingMetricResponse.from(streamingMetricsService.getTodayOrEmpty()));
    }

    @GetMapping("/events")
    public ApiResponse<List<StreamingEventLogResponse>> getEvents() {
        return ApiResponse.success(
                streamingEventLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_EVENTS_LIMIT))
                        .map(StreamingEventLogResponse::from)
                        .getContent()
        );
    }

    @GetMapping("/dead-letter-events")
    public ApiResponse<List<DeadLetterEventResponse>> getDeadLetterEvents() {
        return ApiResponse.success(
                deadLetterEventRepository.findAllByOrderByCreatedAtDesc().stream()
                        .map(DeadLetterEventResponse::from)
                        .toList()
        );
    }

    @PostMapping("/dead-letter-events/{eventId}/retry")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST')")
    public ApiResponse<DeadLetterEventResponse> retryDeadLetterEvent(@PathVariable String eventId) {
        DeadLetterEvent event = findOrThrow(eventId);
        transactionEventProducer.publish(kafkaTopicsProperties.getRawTransactions(), eventId, event.getRawPayload());
        event.setStatus(DeadLetterEventStatus.RETRIED);
        event.setUpdatedAt(LocalDateTime.now());
        DeadLetterEvent saved = deadLetterEventRepository.save(event);

        auditLogService.record(AuditEventType.STREAMING_DEAD_LETTER_RETRIED, "TransactionEvent", eventId,
                null, null, "Dead-lettered event " + eventId + " re-published to raw-transactions");

        return ApiResponse.success("Event requeued", DeadLetterEventResponse.from(saved));
    }

    @PatchMapping("/dead-letter-events/{eventId}/ignore")
    @PreAuthorize("@access.allow('ADMIN', 'ANALYST')")
    public ApiResponse<DeadLetterEventResponse> ignoreDeadLetterEvent(@PathVariable String eventId) {
        DeadLetterEvent event = findOrThrow(eventId);
        event.setStatus(DeadLetterEventStatus.IGNORED);
        event.setUpdatedAt(LocalDateTime.now());
        DeadLetterEvent saved = deadLetterEventRepository.save(event);

        auditLogService.record(AuditEventType.STREAMING_DEAD_LETTER_IGNORED, "TransactionEvent", eventId,
                null, null, "Dead-lettered event " + eventId + " marked ignored");

        return ApiResponse.success("Event ignored", DeadLetterEventResponse.from(saved));
    }

    private DeadLetterEvent findOrThrow(String eventId) {
        return deadLetterEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Dead-letter event not found: " + eventId));
    }
}
