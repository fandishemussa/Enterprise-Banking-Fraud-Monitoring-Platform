package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipelineTaskRunRepository extends JpaRepository<PipelineTaskRun, Long> {

    List<PipelineTaskRun> findByPipelineRunOrderByStartedAtAsc(PipelineRun pipelineRun);
}
