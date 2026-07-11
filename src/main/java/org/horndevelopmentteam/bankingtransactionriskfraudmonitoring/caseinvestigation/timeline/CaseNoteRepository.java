package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseNoteRepository extends JpaRepository<CaseNote, Long> {
    List<CaseNote> findByCaseIdOrderByCreatedAtDesc(String caseId);

    Optional<CaseNote> findByNoteIdAndCaseId(String noteId, String caseId);
}
