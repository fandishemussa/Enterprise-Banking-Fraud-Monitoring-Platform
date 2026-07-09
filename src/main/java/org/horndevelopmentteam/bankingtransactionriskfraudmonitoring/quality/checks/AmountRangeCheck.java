package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmountRangeCheck implements QualityCheck {

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();
        for (CleanTransaction record : records) {
            if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(),
                        "amount must be greater than 0, was " + record.getAmount()));
            }
        }
        int failed = issues.size();
        return new QualityCheckOutcome("AmountRangeCheck", records.size(), records.size() - failed, failed, issues);
    }
}
