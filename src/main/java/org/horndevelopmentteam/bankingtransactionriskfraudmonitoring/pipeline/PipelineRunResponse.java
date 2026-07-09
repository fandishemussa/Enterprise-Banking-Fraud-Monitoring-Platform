package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import java.time.LocalDateTime;
import java.util.List;

public record PipelineRunResponse(
        String runId,
        PipelineType pipelineType,
        PipelineStatus status,
        String triggeredBy,
        int recordsProcessed,
        int recordsAccepted,
        int recordsRejected,
        int recordsFailed,
        Long durationMs,
        String failureReason,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        List<PipelineTaskRunResponse> tasks
) {

    public static PipelineRunResponse from(PipelineRun run, List<PipelineTaskRunResponse> tasks) {
        return new PipelineRunResponse(
                run.getRunId(),
                run.getPipelineType(),
                run.getStatus(),
                run.getTriggeredBy(),
                run.getRecordsProcessed(),
                run.getRecordsAccepted(),
                run.getRecordsRejected(),
                run.getRecordsFailed(),
                run.getDurationMs(),
                run.getFailureReason(),
                run.getStartedAt(),
                run.getFinishedAt(),
                tasks
        );
    }
}
