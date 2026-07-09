package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    Optional<PipelineRun> findByRunId(String runId);

    List<PipelineRun> findTop20ByOrderByStartedAtDesc();

    List<PipelineRun> findByPipelineTypeOrderByStartedAtDesc(PipelineType pipelineType);

    Optional<PipelineRun> findFirstByStatusOrderByFinishedAtDesc(PipelineStatus status);
}
