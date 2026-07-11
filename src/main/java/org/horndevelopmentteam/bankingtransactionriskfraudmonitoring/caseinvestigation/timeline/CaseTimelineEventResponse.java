package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import java.time.LocalDateTime;

public record CaseTimelineEventResponse(
        String eventId,
        String caseId,
        CaseTimelineEventType eventType,
        String title,
        String description,
        String actorUsername,
        String actorRole,
        String metadataJson,
        LocalDateTime createdAt
) {
    public static CaseTimelineEventResponse from(CaseTimelineEvent event) {
        return new CaseTimelineEventResponse(
                event.getEventId(),
                event.getCaseId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getActorUsername(),
                event.getActorRole(),
                event.getMetadataJson(),
                event.getCreatedAt()
        );
    }
}
