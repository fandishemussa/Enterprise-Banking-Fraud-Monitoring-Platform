package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import java.util.List;

public record QualityCheckOutcome(
        String checkName,
        int recordsChecked,
        int recordsPassed,
        int recordsFailed,
        List<Issue> issues
) {

    public record Issue(String recordIdentifier, String description) {
    }

    public boolean passed() {
        return recordsFailed == 0;
    }
}
