package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import java.time.LocalDateTime;

public record CaseNoteResponse(
        String noteId,
        String caseId,
        String authorUsername,
        String authorRole,
        String noteText,
        boolean internalOnly,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CaseNoteResponse from(CaseNote note) {
        return new CaseNoteResponse(
                note.getNoteId(),
                note.getCaseId(),
                note.getAuthorUsername(),
                note.getAuthorRole(),
                note.getNoteText(),
                Boolean.TRUE.equals(note.getInternalOnly()),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
