package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadLetterTransactionRepository extends JpaRepository<DeadLetterTransaction, Long> {

    Optional<DeadLetterTransaction> findByEventId(String eventId);

    List<DeadLetterTransaction> findAllByOrderByReceivedAtDesc();
}
