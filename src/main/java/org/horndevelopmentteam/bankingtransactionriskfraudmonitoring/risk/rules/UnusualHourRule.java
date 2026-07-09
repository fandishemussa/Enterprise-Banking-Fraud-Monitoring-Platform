package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.springframework.stereotype.Component;

@Component
public class UnusualHourRule implements RiskRule {

    private static final int UNUSUAL_HOUR_START = 0;
    private static final int UNUSUAL_HOUR_END = 5;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        int hour = transaction.getCreatedAt().getHour();
        if (hour >= UNUSUAL_HOUR_START && hour <= UNUSUAL_HOUR_END) {
            return new RuleResult("UnusualHourRule", 15, "Transaction occurred during unusual hours (00:00-05:00)");
        }
        return new RuleResult("UnusualHourRule", 0, "Transaction occurred during normal hours");
    }
}
