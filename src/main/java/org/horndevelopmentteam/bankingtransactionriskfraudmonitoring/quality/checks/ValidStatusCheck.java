package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Since {@code status} and {@code transactionType} are persisted as typed Java enums,
 * an invalid value can only occur via out-of-band writes (manual SQL, schema drift) -
 * this check exists to catch exactly that class of drift.
 */
@Component
public class ValidStatusCheck implements QualityCheck {

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();
        for (CleanTransaction record : records) {
            if (record.getStatus() == null) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(), "status is null"));
            }
            if (record.getTransactionType() == null) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(), "transactionType is null"));
            }
        }
        int failed = issues.size();
        return new QualityCheckOutcome("ValidStatusCheck", records.size(), records.size() - failed, failed, issues);
    }
}
