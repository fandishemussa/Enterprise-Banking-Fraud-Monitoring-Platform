package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewCountryRule implements RiskRule {

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        boolean seenBefore = transactionRepository.existsByCustomerAndCountryAndIdNot(
                transaction.getCustomer(), transaction.getCountry(), transaction.getId());
        if (!seenBefore) {
            return new RuleResult("NewCountryRule", 25, "First transaction from this country");
        }
        return new RuleResult("NewCountryRule", 0, "Country previously seen for customer");
    }
}
