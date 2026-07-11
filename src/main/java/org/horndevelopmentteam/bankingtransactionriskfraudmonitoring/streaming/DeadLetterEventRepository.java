package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEvent, Long> {
    List<DeadLetterEvent> findAllByOrderByCreatedAtDesc();

    Optional<DeadLetterEvent> findByEventId(String eventId);
}
