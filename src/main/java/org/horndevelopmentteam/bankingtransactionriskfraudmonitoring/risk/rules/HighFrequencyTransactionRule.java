package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HighFrequencyTransactionRule implements RiskRule {

    private static final String RULE_CODE = "HIGH_FREQUENCY_RULE";
    private static final int WINDOW_MINUTES = 10;
    private static final long DEFAULT_THRESHOLD_COUNT = 5;
    private static final int DEFAULT_SCORE = 35;

    private final TransactionRepository transactionRepository;
    private final FraudRuleConfigService fraudRuleConfigService;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (fraudRuleConfigService.isExplicitlyDisabled(RULE_CODE)) {
            return new RuleResult("HighFrequencyTransactionRule", RULE_CODE, 0, "Rule disabled by configuration", null);
        }

        FraudRule config = fraudRuleConfigService.findEnabledConfig(RULE_CODE).orElse(null);
        long thresholdCount = config != null
                ? config.thresholdAsDecimal(BigDecimal.valueOf(DEFAULT_THRESHOLD_COUNT)).longValue()
                : DEFAULT_THRESHOLD_COUNT;
        int score = config != null ? config.getScoreImpact() : DEFAULT_SCORE;
        RiskLevel severity = config != null ? config.getSeverity() : RiskLevel.HIGH;

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        long recentCount = transactionRepository.countByCustomerAndCreatedAtAfter(
                transaction.getCustomer(), windowStart);
        if (recentCount > thresholdCount) {
            return new RuleResult("HighFrequencyTransactionRule", RULE_CODE, score,
                    "More than " + thresholdCount + " transactions in last " + WINDOW_MINUTES + " minutes", severity);
        }
        return new RuleResult("HighFrequencyTransactionRule", RULE_CODE, 0, "Transaction frequency within normal range", severity);
    }
}
