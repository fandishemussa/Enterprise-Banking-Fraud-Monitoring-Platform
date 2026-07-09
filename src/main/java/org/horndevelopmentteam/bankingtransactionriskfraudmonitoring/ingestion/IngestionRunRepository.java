package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {

    Optional<IngestionRun> findByRunId(String runId);

    List<IngestionRun> findTop20ByOrderByStartedAtDesc();
}
