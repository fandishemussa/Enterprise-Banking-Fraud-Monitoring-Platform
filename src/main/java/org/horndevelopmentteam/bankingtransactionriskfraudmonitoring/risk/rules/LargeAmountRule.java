package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LargeAmountRule implements RiskRule {

    private static final BigDecimal VERY_LARGE_THRESHOLD = BigDecimal.valueOf(10_000);
    private static final BigDecimal LARGE_THRESHOLD = BigDecimal.valueOf(5_000);

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(VERY_LARGE_THRESHOLD) > 0) {
            return new RuleResult("LargeAmountRule", 50, "Amount exceeds 10,000");
        }
        if (amount.compareTo(LARGE_THRESHOLD) > 0) {
            return new RuleResult("LargeAmountRule", 30, "Amount exceeds 5,000");
        }
        return new RuleResult("LargeAmountRule", 0, "Amount within normal range");
    }
}
