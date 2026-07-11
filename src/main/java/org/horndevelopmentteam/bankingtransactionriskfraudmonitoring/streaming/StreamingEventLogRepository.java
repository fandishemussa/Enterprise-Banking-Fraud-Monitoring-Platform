package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingEventLogRepository extends JpaRepository<StreamingEventLog, Long> {
    Page<StreamingEventLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
