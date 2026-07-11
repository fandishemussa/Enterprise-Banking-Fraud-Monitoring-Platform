package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StreamingMetricResponse(
        LocalDate metricDate,
        long eventsProduced,
        long eventsConsumed,
        long eventsFailed,
        long alertsGenerated,
        double averageProcessingLatencyMs,
        LocalDateTime lastEventAt
) {
    public static StreamingMetricResponse from(StreamingMetric metric) {
        return new StreamingMetricResponse(
                metric.getMetricDate(), metric.getEventsProduced(), metric.getEventsConsumed(),
                metric.getEventsFailed(), metric.getAlertsGenerated(), metric.getAverageProcessingLatencyMs(),
                metric.getLastEventAt()
        );
    }
}
