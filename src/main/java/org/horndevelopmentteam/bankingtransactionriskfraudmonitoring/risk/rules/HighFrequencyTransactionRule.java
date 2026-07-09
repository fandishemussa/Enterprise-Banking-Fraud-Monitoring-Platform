package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HighFrequencyTransactionRule implements RiskRule {

    private static final int WINDOW_MINUTES = 10;
    private static final long THRESHOLD_COUNT = 5;

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        long recentCount = transactionRepository.countByCustomerAndCreatedAtAfter(
                transaction.getCustomer(), windowStart);
        if (recentCount > THRESHOLD_COUNT) {
            return new RuleResult("HighFrequencyTransactionRule", 35,
                    "More than " + THRESHOLD_COUNT + " transactions in last " + WINDOW_MINUTES + " minutes");
        }
        return new RuleResult("HighFrequencyTransactionRule", 0, "Transaction frequency within normal range");
    }
}
