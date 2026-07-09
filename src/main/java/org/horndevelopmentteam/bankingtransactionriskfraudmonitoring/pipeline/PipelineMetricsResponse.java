package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import java.time.LocalDateTime;

public record PipelineMetricsResponse(
        long totalRuns,
        long successfulRuns,
        long failedRuns,
        double successRatePercent,
        Double averageDurationMs,
        LocalDateTime lastSuccessfulRunAt,
        String lastFailureReason
) {
}
