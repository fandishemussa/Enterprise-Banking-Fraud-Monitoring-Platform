package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HighRiskMerchantRule implements RiskRule {

    private static final String RULE_CODE = "HIGH_RISK_MERCHANT_RULE";
    private static final Set<String> DEFAULT_HIGH_RISK_CATEGORIES = Set.of("CRYPTO", "GAMBLING", "HIGH_RISK_TRANSFER");
    private static final int DEFAULT_SCORE = 25;

    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("HighRiskMerchantRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        Set<String> highRiskCategories = (config != null && config.getThresholdValue() != null && !config.getThresholdValue().isBlank())
                ? Arrays.stream(config.getThresholdValue().split(",")).map(String::trim).map(String::toUpperCase).collect(java.util.stream.Collectors.toSet())
                : DEFAULT_HIGH_RISK_CATEGORIES;
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.HIGH;

        String merchantCategory = transaction.getMerchantCategory();
        if (merchantCategory != null && highRiskCategories.contains(merchantCategory.toUpperCase())) {
            return new RuleResult("HighRiskMerchantRule", RULE_CODE, score,
                    "Merchant category flagged as high risk: " + merchantCategory, severity);
        }
        return new RuleResult("HighRiskMerchantRule", RULE_CODE, 0, "Merchant category not high risk", severity);
    }
}
