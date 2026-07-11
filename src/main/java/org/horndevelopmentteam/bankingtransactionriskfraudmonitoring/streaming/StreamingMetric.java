package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row per calendar day - counters accumulate through the day rather than being recomputed,
 * so reads stay cheap regardless of event volume. */
@Entity
@Table(name = "streaming_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamingMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate metricDate;

    @Column(nullable = false)
    private long eventsProduced;

    @Column(nullable = false)
    private long eventsConsumed;

    @Column(nullable = false)
    private long eventsFailed;

    @Column(nullable = false)
    private long alertsGenerated;

    @Column(nullable = false)
    private double averageProcessingLatencyMs;

    private LocalDateTime lastEventAt;
}
