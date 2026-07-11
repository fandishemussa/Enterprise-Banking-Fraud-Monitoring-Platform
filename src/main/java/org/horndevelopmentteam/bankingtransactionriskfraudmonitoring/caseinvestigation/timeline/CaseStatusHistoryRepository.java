package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, Long> {
    List<CaseStatusHistory> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
