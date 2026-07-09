package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineRun;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineTaskRun;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineTrackerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.DataQualityService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvIngestionService {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

    private final IngestionRunRepository ingestionRunRepository;
    private final RawTransactionRepository rawTransactionRepository;
    private final StagingTransactionRepository stagingTransactionRepository;
    private final CleanTransactionRepository cleanTransactionRepository;
    private final RejectedTransactionRepository rejectedTransactionRepository;
    private final CsvRowParser csvRowParser;
    private final TransactionCsvValidator validator;
    private final DeadLetterService deadLetterService;
    private final PipelineTrackerService pipelineTrackerService;
    private final DataQualityService dataQualityService;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public IngestionRunResponse ingest(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.csv";
        PipelineRun pipelineRun = pipelineTrackerService.startRun(PipelineType.INGESTION, "csv-upload:" + fileName);

        IngestionRun ingestionRun = ingestionRunRepository.save(IngestionRun.builder()
                .runId(idSequenceService.next("ING"))
                .fileName(fileName)
                .pipelineRun(pipelineRun)
                .status(IngestionStatus.RUNNING)
                .totalRows(0)
                .acceptedRows(0)
                .rejectedRows(0)
                .deadLetterRows(0)
                .startedAt(LocalDateTime.now())
                .build());

        auditLogService.record(
                AuditEventType.DATA_INGESTION_STARTED,
                "IngestionRun",
                ingestionRun.getRunId(),
                null,
                fileName,
                "Ingestion run " + ingestionRun.getRunId() + " started for file " + fileName
        );

        PipelineTaskRun parseTask = pipelineTrackerService.startTask(pipelineRun, "parse-and-stage");
        List<StagingTransaction> stagedRows = new ArrayList<>();
        int totalRows = 0;
        int deadLetterRows = 0;

        List<String> lines = readLines(file);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            totalRows++;
            try {
                CsvRowParser.ParsedRow parsed = csvRowParser.parse(line);
                RawTransaction raw = rawTransactionRepository.save(RawTransaction.builder()
                        .ingestionRun(ingestionRun)
                        .rowNumber(i + 1)
                        .rawLine(line)
                        .transactionIdRaw(parsed.transactionId())
                        .customerIdRaw(parsed.customerId())
                        .sourceAccountIdRaw(parsed.sourceAccountId())
                        .destinationAccountIdRaw(parsed.destinationAccountId())
                        .amountRaw(parsed.amount())
                        .currencyRaw(parsed.currency())
                        .transactionTypeRaw(parsed.transactionType())
                        .channelRaw(parsed.channel())
                        .merchantCategoryRaw(parsed.merchantCategory())
                        .countryRaw(parsed.country())
                        .deviceIdRaw(parsed.deviceId())
                        .ipAddressRaw(parsed.ipAddress())
                        .statusRaw(parsed.status())
                        .createdAtRaw(parsed.createdAt())
                        .ingestedAt(LocalDateTime.now())
                        .build());

                stagedRows.add(stageRow(ingestionRun, raw, parsed));
            } catch (Exception ex) {
                deadLetterRows++;
                deadLetterService.quarantine(ingestionRun, line, ex.getClass().getSimpleName(),
                        ex.getMessage() != null ? ex.getMessage() : "Unparseable row");
            }
        }
        pipelineTrackerService.completeTask(parseTask, totalRows);

        PipelineTaskRun validateTask = pipelineTrackerService.startTask(pipelineRun, "validate-and-load");
        int accepted = 0;
        int rejected = 0;
        for (StagingTransaction staging : stagedRows) {
            TransactionCsvValidator.ValidationResult result = validator.validate(staging);
            if (result == null) {
                cleanTransactionRepository.save(toClean(ingestionRun, staging));
                accepted++;
            } else {
                rejectedTransactionRepository.save(RejectedTransaction.builder()
                        .ingestionRun(ingestionRun)
                        .stagingTransaction(staging)
                        .reason(result.reason())
                        .reasonDetail(result.detail())
                        .rejectedAt(LocalDateTime.now())
                        .build());
                rejected++;
            }
        }
        pipelineTrackerService.completeTask(validateTask, stagedRows.size());

        ingestionRun.setTotalRows(totalRows);
        ingestionRun.setAcceptedRows(accepted);
        ingestionRun.setRejectedRows(rejected);
        ingestionRun.setDeadLetterRows(deadLetterRows);
        ingestionRun.setFinishedAt(LocalDateTime.now());
        ingestionRun.setStatus(deriveStatus(accepted, rejected, deadLetterRows));
        IngestionRun saved = ingestionRunRepository.save(ingestionRun);

        pipelineTrackerService.completeRun(pipelineRun, totalRows, accepted, rejected, deadLetterRows);

        auditLogService.record(
                AuditEventType.DATA_INGESTION_COMPLETED,
                "IngestionRun",
                saved.getRunId(),
                null,
                saved.getStatus().name(),
                "Ingestion run " + saved.getRunId() + " completed: " + accepted + " accepted, "
                        + rejected + " rejected, " + deadLetterRows + " dead-lettered"
        );

        if (accepted > 0) {
            dataQualityService.runChecks("auto-post-ingestion:" + saved.getRunId());
        }

        return IngestionRunResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<IngestionRunResponse> getAllRuns() {
        return ingestionRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(IngestionRunResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RejectedTransactionResponse> getRejectedRecords() {
        return rejectedTransactionRepository.findTop100ByOrderByRejectedAtDesc().stream()
                .map(RejectedTransactionResponse::from)
                .toList();
    }

    private StagingTransaction stageRow(IngestionRun run, RawTransaction raw, CsvRowParser.ParsedRow parsed) {
        StringBuilder parseErrors = new StringBuilder();

        BigDecimal amount = null;
        if (parsed.amount() != null) {
            try {
                amount = new BigDecimal(parsed.amount());
            } catch (NumberFormatException ex) {
                parseErrors.append("amount not numeric: ").append(parsed.amount()).append("; ");
            }
        }

        LocalDateTime createdAt = null;
        if (parsed.createdAt() != null) {
            createdAt = parseDateTime(parsed.createdAt());
            if (createdAt == null) {
                parseErrors.append("createdAt not parseable: ").append(parsed.createdAt()).append("; ");
            }
        }

        StagingTransaction staging = StagingTransaction.builder()
                .ingestionRun(run)
                .rawTransaction(raw)
                .transactionId(parsed.transactionId())
                .customerId(parsed.customerId())
                .sourceAccountId(parsed.sourceAccountId())
                .destinationAccountId(parsed.destinationAccountId())
                .amount(amount)
                .currency(parsed.currency())
                .transactionType(parsed.transactionType())
                .channel(parsed.channel())
                .merchantCategory(parsed.merchantCategory())
                .country(parsed.country())
                .deviceId(parsed.deviceId())
                .ipAddress(parsed.ipAddress())
                .status(parsed.status())
                .createdAt(createdAt)
                .parseErrors(!parseErrors.isEmpty() ? parseErrors.toString() : null)
                .stagedAt(LocalDateTime.now())
                .build();
        return stagingTransactionRepository.save(staging);
    }

    private LocalDateTime parseDateTime(String value) {
        for (DateTimeFormatter format : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private CleanTransaction toClean(IngestionRun run, StagingTransaction staging) {
        return CleanTransaction.builder()
                .ingestionRun(run)
                .transactionId(staging.getTransactionId())
                .customerId(staging.getCustomerId())
                .sourceAccountId(staging.getSourceAccountId())
                .destinationAccountId(staging.getDestinationAccountId())
                .amount(staging.getAmount())
                .currency(staging.getCurrency())
                .transactionType(TransactionType.valueOf(staging.getTransactionType()))
                .channel(TransactionChannel.valueOf(staging.getChannel()))
                .merchantCategory(staging.getMerchantCategory())
                .country(staging.getCountry())
                .deviceId(staging.getDeviceId())
                .ipAddress(staging.getIpAddress())
                .status(TransactionStatus.valueOf(staging.getStatus()))
                .createdAt(staging.getCreatedAt())
                .loadedAt(LocalDateTime.now())
                .build();
    }

    private IngestionStatus deriveStatus(int accepted, int rejected, int deadLetterRows) {
        if (accepted == 0 && (rejected > 0 || deadLetterRows > 0)) {
            return IngestionStatus.FAILED;
        }
        if (rejected > 0 || deadLetterRows > 0) {
            return IngestionStatus.PARTIAL_SUCCESS;
        }
        return IngestionStatus.SUCCESS;
    }

    private List<String> readLines(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        }
    }

    public IngestionRun findByPublicIdOrThrow(String runId) {
        return ingestionRunRepository.findByRunId(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingestion run not found: " + runId));
    }
}
