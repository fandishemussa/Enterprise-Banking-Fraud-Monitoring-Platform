package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewDeviceRule implements RiskRule {

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(BankingTransaction transaction) {
        if (transaction.getDeviceId() == null) {
            return new RuleResult("NewDeviceRule", 0, "No device ID provided");
        }
        boolean seenBefore = transactionRepository.existsByCustomerAndDeviceIdAndIdNot(
                transaction.getCustomer(), transaction.getDeviceId(), transaction.getId());
        if (!seenBefore) {
            return new RuleResult("NewDeviceRule", 20, "First transaction from this device");
        }
        return new RuleResult("NewDeviceRule", 0, "Device previously seen for customer");
    }
}
