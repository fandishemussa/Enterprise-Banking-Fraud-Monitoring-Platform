package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CleanTransactionRepository extends JpaRepository<CleanTransaction, Long> {

    boolean existsByTransactionId(String transactionId);

    List<CleanTransaction> findByCreatedAtAfter(LocalDateTime after);
}
