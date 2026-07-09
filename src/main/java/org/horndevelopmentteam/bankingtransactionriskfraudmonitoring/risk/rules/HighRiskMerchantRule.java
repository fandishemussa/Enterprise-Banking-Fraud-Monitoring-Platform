package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HighRiskMerchantRule implements RiskRule {

    private static final Set<String> HIGH_RISK_CATEGORIES = Set.of("CRYPTO", "GAMBLING", "HIGH_RISK_TRANSFER");

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        String merchantCategory = transaction.getMerchantCategory();
        if (merchantCategory != null && HIGH_RISK_CATEGORIES.contains(merchantCategory.toUpperCase())) {
            return new RuleResult("HighRiskMerchantRule", 25, "Merchant category flagged as high risk: " + merchantCategory);
        }
        return new RuleResult("HighRiskMerchantRule", 0, "Merchant category not high risk");
    }
}
