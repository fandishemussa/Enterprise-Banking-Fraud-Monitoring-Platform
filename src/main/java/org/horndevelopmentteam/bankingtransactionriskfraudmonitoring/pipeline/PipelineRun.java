package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String runId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineType pipelineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineStatus status;

    private String triggeredBy;

    @Column(nullable = false)
    private int recordsProcessed;

    @Column(nullable = false)
    private int recordsAccepted;

    @Column(nullable = false)
    private int recordsRejected;

    @Column(nullable = false)
    private int recordsFailed;

    private Long durationMs;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
