package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataQualityRunRepository extends JpaRepository<DataQualityRun, Long> {

    Optional<DataQualityRun> findByRunId(String runId);

    List<DataQualityRun> findTop20ByOrderByStartedAtDesc();
}
