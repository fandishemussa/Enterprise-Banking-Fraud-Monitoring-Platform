package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import java.time.LocalDateTime;

public record PipelineTaskRunResponse(
        String taskName,
        PipelineStatus status,
        int recordsProcessed,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {

    public static PipelineTaskRunResponse from(PipelineTaskRun task) {
        return new PipelineTaskRunResponse(
                task.getTaskName(),
                task.getStatus(),
                task.getRecordsProcessed(),
                task.getErrorMessage(),
                task.getStartedAt(),
                task.getFinishedAt()
        );
    }
}
