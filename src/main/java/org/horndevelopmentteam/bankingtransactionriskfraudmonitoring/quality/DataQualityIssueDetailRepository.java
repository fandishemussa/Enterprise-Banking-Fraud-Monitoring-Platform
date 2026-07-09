package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataQualityIssueDetailRepository extends JpaRepository<DataQualityIssueDetail, Long> {

    List<DataQualityIssueDetail> findByDataQualityResultOrderByIdAsc(DataQualityResult result);

    List<DataQualityIssueDetail> findTop200ByOrderByDetectedAtDesc();
}
