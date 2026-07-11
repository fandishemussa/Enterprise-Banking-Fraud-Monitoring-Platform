package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseTimelineEventRepository extends JpaRepository<CaseTimelineEvent, Long> {
    List<CaseTimelineEvent> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
