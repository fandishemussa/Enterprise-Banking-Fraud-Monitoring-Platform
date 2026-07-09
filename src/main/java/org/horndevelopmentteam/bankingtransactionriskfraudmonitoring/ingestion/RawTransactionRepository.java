package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RawTransactionRepository extends JpaRepository<RawTransaction, Long> {
}
