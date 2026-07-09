package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;

import java.util.List;

public interface QualityCheck {

    QualityCheckOutcome run(List<CleanTransaction> records);
}
