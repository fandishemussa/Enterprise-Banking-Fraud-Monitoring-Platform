package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

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
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_quality_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataQualityRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String runId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_run_id")
    private PipelineRun pipelineRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PipelineStatus status;

    private String triggeredBy;

    @Column(nullable = false)
    private int totalRecordsChecked;

    @Column(nullable = false)
    private int totalIssuesFound;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
