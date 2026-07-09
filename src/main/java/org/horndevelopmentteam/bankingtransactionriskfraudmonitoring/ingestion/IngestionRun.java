package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineRun;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingestion_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String runId;

    @Column(nullable = false)
    private String fileName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_run_id")
    private PipelineRun pipelineRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngestionStatus status;

    @Column(nullable = false)
    private int totalRows;

    @Column(nullable = false)
    private int acceptedRows;

    @Column(nullable = false)
    private int rejectedRows;

    @Column(nullable = false)
    private int deadLetterRows;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
