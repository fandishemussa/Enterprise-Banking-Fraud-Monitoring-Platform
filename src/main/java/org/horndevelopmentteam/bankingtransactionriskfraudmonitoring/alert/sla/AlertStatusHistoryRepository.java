package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertStatusHistoryRepository extends JpaRepository<AlertStatusHistory, Long> {
    List<AlertStatusHistory> findByAlertIdOrderByCreatedAtDesc(String alertId);
}
