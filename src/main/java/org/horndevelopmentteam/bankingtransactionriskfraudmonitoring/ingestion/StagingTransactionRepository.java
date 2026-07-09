package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StagingTransactionRepository extends JpaRepository<StagingTransaction, Long> {
}
