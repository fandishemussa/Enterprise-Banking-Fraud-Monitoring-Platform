package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shared observability layer for any batch process (CSV ingestion, data quality audits,
 * and later Kafka consumers). Callers start a run, start/finish tasks within it, and
 * record errors; this service owns status derivation and duration bookkeeping.
 */
@Service
@RequiredArgsConstructor
public class PipelineTrackerService {

    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineTaskRunRepository pipelineTaskRunRepository;
    private final PipelineErrorRepository pipelineErrorRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public PipelineRun startRun(PipelineType pipelineType, String triggeredBy) {
        PipelineRun run = PipelineRun.builder()
                .runId(idSequenceService.next("PIPE"))
                .pipelineType(pipelineType)
                .status(PipelineStatus.RUNNING)
                .triggeredBy(triggeredBy)
                .recordsProcessed(0)
                .recordsAccepted(0)
                .recordsRejected(0)
                .recordsFailed(0)
                .startedAt(LocalDateTime.now())
                .build();
        return pipelineRunRepository.save(run);
    }

    @Transactional
    public PipelineTaskRun startTask(PipelineRun run, String taskName) {
        PipelineTaskRun task = PipelineTaskRun.builder()
                .pipelineRun(run)
                .taskName(taskName)
                .status(PipelineStatus.RUNNING)
                .recordsProcessed(0)
                .startedAt(LocalDateTime.now())
                .build();
        return pipelineTaskRunRepository.save(task);
    }

    @Transactional
    public void completeTask(PipelineTaskRun task, int recordsProcessed) {
        task.setStatus(PipelineStatus.SUCCESS);
        task.setRecordsProcessed(recordsProcessed);
        task.setFinishedAt(LocalDateTime.now());
        pipelineTaskRunRepository.save(task);
    }

    @Transactional
    public void failTask(PipelineTaskRun task, String errorType, String errorMessage) {
        task.setStatus(PipelineStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(LocalDateTime.now());
        pipelineTaskRunRepository.save(task);
        recordError(task.getPipelineRun(), task, errorType, errorMessage);
    }

    @Transactional
    public void recordError(PipelineRun run, PipelineTaskRun task, String errorType, String errorMessage) {
        PipelineError error = PipelineError.builder()
                .pipelineRun(run)
                .pipelineTaskRun(task)
                .errorType(errorType)
                .errorMessage(errorMessage)
                .occurredAt(LocalDateTime.now())
                .build();
        pipelineErrorRepository.save(error);

        auditLogService.record(
                AuditEventType.PIPELINE_FAILED,
                "PipelineRun",
                run.getRunId(),
                null,
                errorType,
                "Pipeline " + run.getRunId() + " task error: " + errorMessage
        );
    }

    @Transactional
    public PipelineRun completeRun(PipelineRun run, int processed, int accepted, int rejected, int failed) {
        run.setRecordsProcessed(processed);
        run.setRecordsAccepted(accepted);
        run.setRecordsRejected(rejected);
        run.setRecordsFailed(failed);
        run.setFinishedAt(LocalDateTime.now());
        run.setDurationMs(Duration.between(run.getStartedAt(), run.getFinishedAt()).toMillis());

        if (failed > 0 && accepted == 0) {
            run.setStatus(PipelineStatus.FAILED);
            run.setFailureReason(failed + " record(s) failed processing");
        } else if (failed > 0 || rejected > 0) {
            run.setStatus(PipelineStatus.PARTIAL_SUCCESS);
        } else {
            run.setStatus(PipelineStatus.SUCCESS);
        }

        return pipelineRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public List<PipelineTaskRun> getTasksForRun(PipelineRun run) {
        return pipelineTaskRunRepository.findByPipelineRunOrderByStartedAtAsc(run);
    }
}
