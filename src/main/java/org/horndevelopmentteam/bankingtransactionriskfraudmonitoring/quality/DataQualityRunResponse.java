package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineStatus;

import java.time.LocalDateTime;

public record DataQualityRunResponse(
        String runId,
        PipelineStatus status,
        String triggeredBy,
        int totalRecordsChecked,
        int totalIssuesFound,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {

    public static DataQualityRunResponse from(DataQualityRun run) {
        return new DataQualityRunResponse(
                run.getRunId(),
                run.getStatus(),
                run.getTriggeredBy(),
                run.getTotalRecordsChecked(),
                run.getTotalIssuesFound(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }
}
