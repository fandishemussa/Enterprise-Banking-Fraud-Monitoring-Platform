package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {
    Optional<FraudRule> findByRuleId(String ruleId);

    Optional<FraudRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    List<FraudRule> findByRuleTypeAndEnabledTrue(FraudRuleType ruleType);
}
