package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * Fraud-ring signal: flags a transaction when its device ID or IP address was already used by a
 * DIFFERENT customer (NewDeviceRule only checks whether this SAME customer has used the device
 * before - this rule is the cross-customer counterpart). A device or IP shared across otherwise
 * unrelated customers is a classic indicator of a coordinated fraud ring or a stolen-credentials
 * "device farm", not just one customer's risky behavior.
 */
@Component
@RequiredArgsConstructor
public class SharedDeviceOrIpRule implements RiskRule {

    private static final String RULE_CODE = "SHARED_DEVICE_IP_RULE";
    private static final int DEFAULT_SCORE = 30;

    private final TransactionRepository transactionRepository;
    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("SharedDeviceOrIpRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.HIGH;

        boolean sharedDevice = transaction.getDeviceId() != null
                && transactionRepository.existsByDeviceIdAndCustomerNot(transaction.getDeviceId(), transaction.getCustomer());
        boolean sharedIp = transaction.getIpAddress() != null
                && transactionRepository.existsByIpAddressAndCustomerNot(transaction.getIpAddress(), transaction.getCustomer());

        if (sharedDevice && sharedIp) {
            return new RuleResult("SharedDeviceOrIpRule", RULE_CODE, score,
                    "Device and IP both used by another customer - possible fraud ring", severity);
        }
        if (sharedDevice) {
            return new RuleResult("SharedDeviceOrIpRule", RULE_CODE, score,
                    "Device used by another customer - possible fraud ring", severity);
        }
        if (sharedIp) {
            return new RuleResult("SharedDeviceOrIpRule", RULE_CODE, score,
                    "IP address used by another customer - possible fraud ring", severity);
        }
        return new RuleResult("SharedDeviceOrIpRule", RULE_CODE, 0, "No device/IP overlap with other customers", severity);
    }
}
