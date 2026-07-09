package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotNullCheck implements QualityCheck {

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();
        for (CleanTransaction record : records) {
            if (record.getCustomerId() == null || record.getCustomerId().isBlank()) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(), "customer_id is null or blank"));
            }
        }
        int failed = issues.size();
        return new QualityCheckOutcome("NotNullCheck", records.size(), records.size() - failed, failed, issues);
    }
}
