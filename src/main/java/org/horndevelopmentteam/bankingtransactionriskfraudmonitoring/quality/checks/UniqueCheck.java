package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UniqueCheck implements QualityCheck {

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        Map<String, Integer> countsByTransactionId = new HashMap<>();
        for (CleanTransaction record : records) {
            countsByTransactionId.merge(record.getTransactionId(), 1, Integer::sum);
        }

        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();
        for (CleanTransaction record : records) {
            int count = countsByTransactionId.get(record.getTransactionId());
            if (count > 1) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(),
                        "transaction_id appears " + count + " times in clean_transactions"));
            }
        }
        int failed = issues.size();
        return new QualityCheckOutcome("UniqueCheck", records.size(), records.size() - failed, failed, issues);
    }
}
