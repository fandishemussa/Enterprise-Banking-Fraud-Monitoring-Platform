package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudRuleVersionRepository extends JpaRepository<FraudRuleVersion, Long> {
    List<FraudRuleVersion> findByRuleIdOrderByVersionNumberDesc(String ruleId);

    int countByRuleId(String ruleId);
}
