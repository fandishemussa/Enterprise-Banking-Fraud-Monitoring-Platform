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
public class LargeAmountRule implements RiskRule {

    private static final String RULE_CODE = "LARGE_AMOUNT_RULE";
    private static final BigDecimal DEFAULT_LARGE_THRESHOLD = BigDecimal.valueOf(5_000);
    private static final BigDecimal DEFAULT_VERY_LARGE_THRESHOLD = BigDecimal.valueOf(10_000);
    private static final int DEFAULT_SCORE = 30;
    private static final int DEFAULT_SECONDARY_SCORE = 50;

    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("LargeAmountRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        BigDecimal largeThreshold = config != null ? config.thresholdAsDecimal(DEFAULT_LARGE_THRESHOLD) : DEFAULT_LARGE_THRESHOLD;
        BigDecimal veryLargeThreshold = config != null ? config.secondaryThresholdAsDecimal(DEFAULT_VERY_LARGE_THRESHOLD) : DEFAULT_VERY_LARGE_THRESHOLD;
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        int secondaryScore = config != null && config.getSecondaryScoreImpact() != null ? config.getSecondaryScoreImpact() : DEFAULT_SECONDARY_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.HIGH;

        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(veryLargeThreshold) > 0) {
            return new RuleResult("LargeAmountRule", RULE_CODE, secondaryScore,
                    "Amount exceeds " + veryLargeThreshold, severity);
        }
        if (amount.compareTo(largeThreshold) > 0) {
            return new RuleResult("LargeAmountRule", RULE_CODE, score,
                    "Amount exceeds " + largeThreshold, severity);
        }
        return new RuleResult("LargeAmountRule", RULE_CODE, 0, "Amount within normal range", severity);
    }
}
