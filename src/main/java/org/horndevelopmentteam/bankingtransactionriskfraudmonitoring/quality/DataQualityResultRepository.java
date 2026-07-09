package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataQualityResultRepository extends JpaRepository<DataQualityResult, Long> {

    List<DataQualityResult> findByDataQualityRunOrderByIdAsc(DataQualityRun run);

    List<DataQualityResult> findTop100ByOrderByIdDesc();
}
