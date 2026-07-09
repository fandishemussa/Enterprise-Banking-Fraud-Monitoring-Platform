package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import java.time.LocalDateTime;

public record IngestionRunResponse(
        String runId,
        String fileName,
        IngestionStatus status,
        int totalRows,
        int acceptedRows,
        int rejectedRows,
        int deadLetterRows,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {

    public static IngestionRunResponse from(IngestionRun run) {
        return new IngestionRunResponse(
                run.getRunId(),
                run.getFileName(),
                run.getStatus(),
                run.getTotalRows(),
                run.getAcceptedRows(),
                run.getRejectedRows(),
                run.getDeadLetterRows(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }
}
