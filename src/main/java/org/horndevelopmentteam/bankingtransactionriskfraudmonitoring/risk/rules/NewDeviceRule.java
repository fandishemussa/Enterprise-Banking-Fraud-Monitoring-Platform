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
public class NewDeviceRule implements RiskRule {

    private static final String RULE_CODE = "NEW_DEVICE_RULE";
    private static final int DEFAULT_SCORE = 20;

    private final TransactionRepository transactionRepository;
    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("NewDeviceRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }
        if (transaction.getDeviceId() == null) {
            return new RuleResult("NewDeviceRule", RULE_CODE, 0, "No device ID provided", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.MEDIUM;

        boolean seenBefore = transactionRepository.existsByCustomerAndDeviceIdAndIdNot(
                transaction.getCustomer(), transaction.getDeviceId(), transaction.getId());
        if (!seenBefore) {
            return new RuleResult("NewDeviceRule", RULE_CODE, score, "First transaction from this device", severity);
        }
        return new RuleResult("NewDeviceRule", RULE_CODE, 0, "Device previously seen for customer", severity);
    }
}
