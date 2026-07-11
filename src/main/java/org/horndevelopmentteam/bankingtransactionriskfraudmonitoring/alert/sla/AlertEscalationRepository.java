package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertEscalationRepository extends JpaRepository<AlertEscalation, Long> {
    List<AlertEscalation> findByAlertIdOrderByCreatedAtDesc(String alertId);

    int countByAlertId(String alertId);

    List<AlertEscalation> findByEscalatedToAndCreatedAtAfter(String escalatedTo, LocalDateTime after);
}
