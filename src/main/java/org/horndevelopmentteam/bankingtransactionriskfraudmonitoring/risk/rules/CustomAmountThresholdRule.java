package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic amount-threshold evaluator for admin-created CUSTOM fraud rules (see
 * risk.rulesmanagement.FraudRuleController#create). Unlike the six built-in rules, which each look
 * themselves up by a fixed ruleCode, this rule evaluates every enabled row with ruleType=CUSTOM so an
 * admin can add new amount-based checks from the Risk Rules UI without a code change. Each triggered
 * custom rule contributes its own scoreImpact/secondaryScoreImpact (same threshold/secondaryThreshold
 * semantics as LargeAmountRule), summed into one combined result.
 */
@Component
@RequiredArgsConstructor
public class CustomAmountThresholdRule implements RiskRule {

    private final FraudRuleRepository fraudRuleRepository;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        List<FraudRule> customRules = fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM);
        if (customRules.isEmpty()) {
            return new RuleResult("CustomAmountThresholdRule", null, 0, "No custom rules configured", null);
        }

        BigDecimal amount = transaction.getAmount();
        int totalPoints = 0;
        List<String> triggeredCodes = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        RiskLevel highestSeverity = null;

        for (FraudRule rule : customRules) {
            BigDecimal threshold = rule.thresholdAsDecimal(null);
            if (threshold == null) continue;

            BigDecimal secondaryThreshold = rule.secondaryThresholdAsDecimal(null);
            int points = 0;
            String reason = null;
            if (secondaryThreshold != null && amount.compareTo(secondaryThreshold) > 0) {
                points = rule.getSecondaryScoreImpact() != null ? rule.getSecondaryScoreImpact() : rule.getScoreImpact();
                reason = rule.getName() + ": amount exceeds " + secondaryThreshold;
            } else if (amount.compareTo(threshold) > 0) {
                points = rule.getScoreImpact();
                reason = rule.getName() + ": amount exceeds " + threshold;
            }

            if (points > 0) {
                totalPoints += points;
                triggeredCodes.add(rule.getRuleCode());
                reasons.add(reason);
                if (highestSeverity == null || rule.getSeverity().ordinal() > highestSeverity.ordinal()) {
                    highestSeverity = rule.getSeverity();
                }
            }
        }

        if (totalPoints == 0) {
            return new RuleResult("CustomAmountThresholdRule", null, 0, "No custom rules triggered", null);
        }

        return new RuleResult(
                "CustomAmountThresholdRule",
                String.join(",", triggeredCodes),
                totalPoints,
                String.join("; ", reasons),
                highestSeverity);
    }
}
