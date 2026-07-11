package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewCountryRule implements RiskRule {

    private static final String RULE_CODE = "NEW_COUNTRY_RULE";
    private static final int DEFAULT_SCORE = 25;

    private final TransactionRepository transactionRepository;
    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("NewCountryRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.HIGH;

        boolean seenBefore = transactionRepository.existsByCustomerAndCountryAndIdNot(
                transaction.getCustomer(), transaction.getCountry(), transaction.getId());
        if (!seenBefore) {
            return new RuleResult("NewCountryRule", RULE_CODE, score, "First transaction from this country", severity);
        }
        return new RuleResult("NewCountryRule", RULE_CODE, 0, "Country previously seen for customer", severity);
    }
}
