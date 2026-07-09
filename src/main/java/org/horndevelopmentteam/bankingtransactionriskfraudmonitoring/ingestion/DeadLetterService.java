package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadLetterService {

    private final DeadLetterTransactionRepository deadLetterTransactionRepository;
    private final IdSequenceService idSequenceService;
    private final CsvRowParser csvRowParser;

    @Transactional
    public DeadLetterTransaction quarantine(IngestionRun ingestionRun, String rawPayload, String errorType, String errorReason) {
        DeadLetterTransaction entry = DeadLetterTransaction.builder()
                .eventId(idSequenceService.next("DLQ"))
                .ingestionRun(ingestionRun)
                .rawPayload(rawPayload)
                .errorType(errorType)
                .errorReason(errorReason)
                .processedStatus(DeadLetterStatus.NEW)
                .receivedAt(LocalDateTime.now())
                .build();
        return deadLetterTransactionRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<DeadLetterResponse> getAll() {
        return deadLetterTransactionRepository.findAllByOrderByReceivedAtDesc().stream()
                .map(DeadLetterResponse::from)
                .toList();
    }

    /**
     * Re-attempts structural parsing of the quarantined raw CSV line. Note this only confirms the
     * row is now well-formed (correct column count) - it does not re-run business validation or
     * promote the row to clean_transactions, since that requires re-running the full ingestion
     * pipeline. Use CSV re-upload for a full replay.
     */
    @Transactional
    public DeadLetterResponse retry(String eventId) {
        DeadLetterTransaction entry = findByPublicIdOrThrow(eventId);
        try {
            csvRowParser.parse(entry.getRawPayload());
            entry.setProcessedStatus(DeadLetterStatus.RESOLVED);
        } catch (Exception ex) {
            entry.setProcessedStatus(DeadLetterStatus.RETRIED);
            entry.setErrorReason(ex.getMessage() != null ? ex.getMessage() : "Retry failed");
        }
        entry.setLastRetryAt(LocalDateTime.now());
        return DeadLetterResponse.from(deadLetterTransactionRepository.save(entry));
    }

    @Transactional
    public DeadLetterResponse ignore(String eventId) {
        DeadLetterTransaction entry = findByPublicIdOrThrow(eventId);
        entry.setProcessedStatus(DeadLetterStatus.IGNORED);
        return DeadLetterResponse.from(deadLetterTransactionRepository.save(entry));
    }

    private DeadLetterTransaction findByPublicIdOrThrow(String eventId) {
        return deadLetterTransactionRepository.findByEventId(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Dead letter entry not found: " + eventId));
    }
}
