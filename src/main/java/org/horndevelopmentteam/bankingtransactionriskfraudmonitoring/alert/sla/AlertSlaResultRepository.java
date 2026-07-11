package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertSlaResultRepository extends JpaRepository<AlertSlaResult, Long> {
    Optional<AlertSlaResult> findByAlertId(String alertId);

    List<AlertSlaResult> findByStatus(SlaStatus status);

    List<AlertSlaResult> findByStatusIn(List<SlaStatus> statuses);
}
