package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertSlaPolicyRepository extends JpaRepository<AlertSlaPolicy, Long> {
    Optional<AlertSlaPolicy> findByPolicyId(String policyId);

    Optional<AlertSlaPolicy> findByPriority(AlertPriority priority);

    boolean existsByPriority(AlertPriority priority);
}
