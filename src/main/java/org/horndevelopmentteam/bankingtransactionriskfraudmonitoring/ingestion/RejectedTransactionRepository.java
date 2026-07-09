package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RejectedTransactionRepository extends JpaRepository<RejectedTransaction, Long> {

    List<RejectedTransaction> findByIngestionRunOrderByRejectedAtAsc(IngestionRun ingestionRun);

    List<RejectedTransaction> findTop100ByOrderByRejectedAtDesc();
}
