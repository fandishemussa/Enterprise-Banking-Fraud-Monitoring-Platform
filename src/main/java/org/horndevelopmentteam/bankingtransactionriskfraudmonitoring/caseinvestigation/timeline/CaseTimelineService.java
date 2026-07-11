package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Records everything that happens to an InvestigationCase - timeline events, analyst notes, and
 * status-change history - and is called from InvestigationCaseService at each of those points
 * rather than duplicating case-mutation logic here. This service only ever appends rows; it never
 * mutates the case itself.
 */
@Service
@RequiredArgsConstructor
public class CaseTimelineService {

    private final CaseNoteRepository caseNoteRepository;
    private final CaseTimelineEventRepository caseTimelineEventRepository;
    private final CaseStatusHistoryRepository caseStatusHistoryRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public void recordEvent(String caseId, CaseTimelineEventType eventType, String title, String description,
                             String actorUsername, String actorRole) {
        CaseTimelineEvent event = CaseTimelineEvent.builder()
                .eventId(idSequenceService.next("EVENT"))
                .caseId(caseId)
                .eventType(eventType)
                .title(title)
                .description(description)
                .actorUsername(actorUsername)
                .actorRole(actorRole)
                .createdAt(LocalDateTime.now())
                .build();
        caseTimelineEventRepository.save(event);

        auditLogService.record(AuditEventType.CASE_TIMELINE_EVENT_CREATED, "InvestigationCase", caseId,
                null, eventType.name(), title);
    }

    @Transactional
    public void recordStatusChange(String caseId, String oldStatus, String newStatus, String changedBy, String reason) {
        CaseStatusHistory history = CaseStatusHistory.builder()
                .caseId(caseId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        caseStatusHistoryRepository.save(history);

        auditLogService.record(AuditEventType.CASE_STATUS_HISTORY_CREATED, "InvestigationCase", caseId,
                oldStatus, newStatus, "Case " + caseId + " status changed from " + oldStatus + " to " + newStatus);
    }

    @Transactional
    public CaseNoteResponse addNote(String caseId, CaseNoteRequest request, String actorUsername, String actorRole) {
        LocalDateTime now = LocalDateTime.now();
        CaseNote note = CaseNote.builder()
                .noteId(idSequenceService.next("NOTE"))
                .caseId(caseId)
                .authorUsername(actorUsername)
                .authorRole(actorRole)
                .noteText(request.noteText())
                .internalOnly(request.internalOnly() != null && request.internalOnly())
                .createdAt(now)
                .updatedAt(now)
                .build();
        CaseNote saved = caseNoteRepository.save(note);

        auditLogService.record(AuditEventType.CASE_NOTE_ADDED, "CaseNote", saved.getNoteId(),
                null, null, "Note " + saved.getNoteId() + " added to case " + caseId + " by " + actorUsername);

        recordEvent(caseId, CaseTimelineEventType.NOTE_ADDED, "Note added",
                truncate(request.noteText()), actorUsername, actorRole);

        return CaseNoteResponse.from(saved);
    }

    @Transactional
    public CaseNoteResponse updateNote(String caseId, String noteId, CaseNoteRequest request, String actorUsername, String actorRole) {
        CaseNote note = findNoteOrThrow(caseId, noteId);
        note.setNoteText(request.noteText());
        if (request.internalOnly() != null) {
            note.setInternalOnly(request.internalOnly());
        }
        note.setUpdatedAt(LocalDateTime.now());
        CaseNote saved = caseNoteRepository.save(note);

        auditLogService.record(AuditEventType.CASE_NOTE_UPDATED, "CaseNote", saved.getNoteId(),
                null, null, "Note " + saved.getNoteId() + " updated by " + actorUsername);

        recordEvent(caseId, CaseTimelineEventType.NOTE_UPDATED, "Note updated",
                truncate(request.noteText()), actorUsername, actorRole);

        return CaseNoteResponse.from(saved);
    }

    @Transactional
    public void deleteNote(String caseId, String noteId, String actorUsername) {
        CaseNote note = findNoteOrThrow(caseId, noteId);
        caseNoteRepository.delete(note);

        auditLogService.record(AuditEventType.CASE_NOTE_DELETED, "CaseNote", noteId,
                null, null, "Note " + noteId + " deleted from case " + caseId + " by " + actorUsername);
    }

    @Transactional(readOnly = true)
    public List<CaseTimelineEventResponse> getTimeline(String caseId) {
        return caseTimelineEventRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(CaseTimelineEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaseNoteResponse> getNotes(String caseId) {
        return caseNoteRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(CaseNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaseStatusHistoryResponse> getStatusHistory(String caseId) {
        return caseStatusHistoryRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(CaseStatusHistoryResponse::from)
                .toList();
    }

    private CaseNote findNoteOrThrow(String caseId, String noteId) {
        return caseNoteRepository.findByNoteIdAndCaseId(noteId, caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case note not found: " + noteId));
    }

    private String truncate(String text) {
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }
}
