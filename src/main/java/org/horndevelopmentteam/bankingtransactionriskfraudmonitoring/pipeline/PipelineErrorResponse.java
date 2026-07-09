package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import java.time.LocalDateTime;

public record PipelineErrorResponse(
        String pipelineRunId,
        String taskName,
        String errorType,
        String errorMessage,
        LocalDateTime occurredAt
) {

    public static PipelineErrorResponse from(PipelineError error) {
        return new PipelineErrorResponse(
                error.getPipelineRun().getRunId(),
                error.getPipelineTaskRun() != null ? error.getPipelineTaskRun().getTaskName() : null,
                error.getErrorType(),
                error.getErrorMessage(),
                error.getOccurredAt()
        );
    }
}
