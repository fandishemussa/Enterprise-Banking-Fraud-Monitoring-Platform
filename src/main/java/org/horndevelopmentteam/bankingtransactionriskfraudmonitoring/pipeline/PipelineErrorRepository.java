package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineErrorRepository extends JpaRepository<PipelineError, Long> {

    List<PipelineError> findTop50ByOrderByOccurredAtDesc();

    List<PipelineError> findByPipelineRunOrderByOccurredAtAsc(PipelineRun pipelineRun);
}
