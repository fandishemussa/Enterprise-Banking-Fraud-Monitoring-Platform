package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Flags individual records whose createdAt is in the future, and separately flags the whole
 * batch as stale if the most recent record is older than {@link #STALE_AFTER_HOURS} hours.
 */
@Component
public class FreshnessCheck implements QualityCheck {

    private static final long STALE_AFTER_HOURS = 24 * 7;

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        LocalDateTime now = LocalDateTime.now();
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();

        for (CleanTransaction record : records) {
            if (record.getCreatedAt() != null && record.getCreatedAt().isAfter(now)) {
                issues.add(new QualityCheckOutcome.Issue(record.getTransactionId(),
                        "createdAt is in the future: " + record.getCreatedAt()));
            }
        }

        Optional<LocalDateTime> mostRecent = records.stream()
                .map(CleanTransaction::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo);

        if (mostRecent.isPresent() && mostRecent.get().isBefore(now.minusHours(STALE_AFTER_HOURS))) {
            issues.add(new QualityCheckOutcome.Issue(null,
                    "Most recent clean_transactions record is from " + mostRecent.get()
                            + ", older than the " + STALE_AFTER_HOURS + "h freshness threshold"));
        }

        int failed = issues.size();
        return new QualityCheckOutcome("FreshnessCheck", records.size(), records.size() - failed, failed, issues);
    }
}
