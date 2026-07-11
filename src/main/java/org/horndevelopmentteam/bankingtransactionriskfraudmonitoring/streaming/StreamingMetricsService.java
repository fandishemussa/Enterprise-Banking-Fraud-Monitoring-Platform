package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row per calendar day (see StreamingMetric) - all increments here go through a
 * find-or-create-today's-row path so callers don't need to know about the daily bucketing. */
@Service
@RequiredArgsConstructor
public class StreamingMetricsService {

    private final StreamingMetricRepository streamingMetricRepository;

    @Transactional
    public void recordProduced() {
        StreamingMetric metric = todayMetric();
        metric.setEventsProduced(metric.getEventsProduced() + 1);
        metric.setLastEventAt(LocalDateTime.now());
        streamingMetricRepository.save(metric);
    }

    @Transactional
    public void recordConsumed(long latencyMs, boolean alertGenerated) {
        StreamingMetric metric = todayMetric();
        long previousCount = metric.getEventsConsumed();
        double previousAverage = metric.getAverageProcessingLatencyMs();
        metric.setEventsConsumed(previousCount + 1);
        metric.setAverageProcessingLatencyMs(
                ((previousAverage * previousCount) + latencyMs) / (previousCount + 1));
        if (alertGenerated) {
            metric.setAlertsGenerated(metric.getAlertsGenerated() + 1);
        }
        metric.setLastEventAt(LocalDateTime.now());
        streamingMetricRepository.save(metric);
    }

    @Transactional
    public void recordFailed() {
        StreamingMetric metric = todayMetric();
        metric.setEventsFailed(metric.getEventsFailed() + 1);
        metric.setLastEventAt(LocalDateTime.now());
        streamingMetricRepository.save(metric);
    }

    @Transactional(readOnly = true)
    public StreamingMetric getTodayOrEmpty() {
        return streamingMetricRepository.findByMetricDate(LocalDate.now())
                .orElseGet(() -> StreamingMetric.builder()
                        .metricDate(LocalDate.now())
                        .eventsProduced(0).eventsConsumed(0).eventsFailed(0).alertsGenerated(0)
                        .averageProcessingLatencyMs(0)
                        .build());
    }

    private StreamingMetric todayMetric() {
        return streamingMetricRepository.findByMetricDate(LocalDate.now())
                .orElseGet(() -> StreamingMetric.builder()
                        .metricDate(LocalDate.now())
                        .eventsProduced(0).eventsConsumed(0).eventsFailed(0).alertsGenerated(0)
                        .averageProcessingLatencyMs(0)
                        .build());
    }
}
