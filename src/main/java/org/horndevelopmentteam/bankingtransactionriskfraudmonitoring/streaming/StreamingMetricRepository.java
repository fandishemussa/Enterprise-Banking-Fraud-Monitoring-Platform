package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StreamingMetricRepository extends JpaRepository<StreamingMetric, Long> {
    Optional<StreamingMetric> findByMetricDate(LocalDate metricDate);
}
