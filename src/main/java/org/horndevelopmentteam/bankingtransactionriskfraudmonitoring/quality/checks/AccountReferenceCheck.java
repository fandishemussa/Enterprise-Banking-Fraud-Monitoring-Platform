package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccountReferenceCheck implements QualityCheck {

    private final AccountRepository accountRepository;

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        Map<String, Boolean> existenceCache = new HashMap<>();
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();

        for (CleanTransaction record : records) {
            boolean exists = existenceCache.computeIfAbsent(record.getSourceAccountId(),
                    id -> accountRepository.findByAccountId(id).isPresent());
            if (!exists) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(),
                        "sourceAccountId " + record.getSourceAccountId() + " does not exist"));
            }
        }
        int failed = issues.size();
        return new QualityCheckOutcome("AccountReferenceCheck", records.size(), records.size() - failed, failed, issues);
    }
}
