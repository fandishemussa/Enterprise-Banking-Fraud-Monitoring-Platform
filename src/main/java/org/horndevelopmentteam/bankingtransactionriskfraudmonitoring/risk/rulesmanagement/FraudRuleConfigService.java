package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Read-only lookup used by risk.rules.RiskRule implementations to pull their configuration
 * (threshold, score impact, enabled flag) from the database instead of the hardcoded constants
 * they used to have. If a rule's row is missing entirely (e.g. a fresh DB before the seeder has
 * run, or a ruleCode typo), callers get an empty Optional and are expected to fall back to their
 * own hardcoded default while logging a warning - scoring must never hard-fail just because rule
 * configuration is temporarily unavailable.
 */
@Service
@RequiredArgsConstructor
public class FraudRuleConfigService {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleConfigService.class);

    private final FraudRuleRepository fraudRuleRepository;

    @Transactional(readOnly = true)
    public Optional<FraudRule> findEnabledConfig(String ruleCode) {
        Optional<FraudRule> rule = fraudRuleRepository.findByRuleCode(ruleCode);
        if (rule.isEmpty()) {
            log.warn("No FraudRule configuration found for ruleCode '{}' - rule will use its hardcoded fallback", ruleCode);
        }
        return rule;
    }

    /** True only when a configuration row exists AND is disabled - missing configuration should
     * NOT disable a rule (that would silently turn off fraud detection on a fresh/incomplete DB). */
    @Transactional(readOnly = true)
    public boolean isExplicitlyDisabled(String ruleCode) {
        return fraudRuleRepository.findByRuleCode(ruleCode)
                .map(rule -> !Boolean.TRUE.equals(rule.getEnabled()))
                .orElse(false);
    }
}
