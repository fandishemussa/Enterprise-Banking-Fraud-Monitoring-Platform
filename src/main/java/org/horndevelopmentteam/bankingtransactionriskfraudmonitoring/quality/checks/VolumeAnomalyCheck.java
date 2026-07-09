package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.IngestionRunRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares the current clean_transactions volume against the average of the last few ingestion
 * runs' accepted-row counts, flagging a swing of more than {@link #ANOMALY_THRESHOLD_PERCENT}%
 * as a possible upstream problem (e.g. a partial file, a duplicate re-send, a broken feed).
 */
@Component
@RequiredArgsConstructor
public class VolumeAnomalyCheck implements QualityCheck {

    private static final double ANOMALY_THRESHOLD_PERCENT = 50.0;

    private final IngestionRunRepository ingestionRunRepository;

    @Override
    public QualityCheckOutcome run(List<CleanTransaction> records) {
        List<QualityCheckOutcome.Issue> issues = new ArrayList<>();

        List<Integer> recentAcceptedCounts = ingestionRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(run -> run.getAcceptedRows())
                .toList();

        if (recentAcceptedCounts.size() >= 2) {
            double average = recentAcceptedCounts.stream().mapToInt(Integer::intValue).average().orElse(0);
            int latest = recentAcceptedCounts.get(0);
            if (average > 0) {
                double deviationPercent = Math.abs(latest - average) / average * 100.0;
                if (deviationPercent > ANOMALY_THRESHOLD_PERCENT) {
                    issues.add(new QualityCheckOutcome.Issue(null,
                            "Latest ingestion accepted " + latest + " rows, deviating "
                                    + String.format("%.1f", deviationPercent)
                                    + "% from the recent average of " + String.format("%.1f", average)));
                }
            }
        }

        int failed = issues.size();
        return new QualityCheckOutcome("VolumeAnomalyCheck", records.size(), records.size() - failed, failed, issues);
    }
}
