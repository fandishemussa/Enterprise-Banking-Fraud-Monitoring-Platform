package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class UnusualHourRule implements RiskRule {

    private static final String RULE_CODE = "UNUSUAL_HOUR_RULE";
    private static final int DEFAULT_START_HOUR = 0;
    private static final int DEFAULT_END_HOUR = 5;
    private static final int DEFAULT_SCORE = 15;

    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("UnusualHourRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        int startHour = config != null ? config.thresholdAsDecimal(BigDecimal.valueOf(DEFAULT_START_HOUR)).intValue() : DEFAULT_START_HOUR;
        int endHour = config != null ? config.secondaryThresholdAsDecimal(BigDecimal.valueOf(DEFAULT_END_HOUR)).intValue() : DEFAULT_END_HOUR;
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.MEDIUM;

        int hour = transaction.getCreatedAt().getHour();
        if (hour >= startHour && hour <= endHour) {
            return new RuleResult("UnusualHourRule", RULE_CODE, score,
                    "Transaction occurred during unusual hours (" + startHour + ":00-" + endHour + ":00)", severity);
        }
        return new RuleResult("UnusualHourRule", RULE_CODE, 0, "Transaction occurred during normal hours", severity);
    }
}
