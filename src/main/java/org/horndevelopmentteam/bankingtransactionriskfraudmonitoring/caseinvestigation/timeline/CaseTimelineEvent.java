package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_timeline_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private String caseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CaseTimelineEventType eventType;

    @Column(nullable = false, updatable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(updatable = false)
    private String actorUsername;

    @Column(updatable = false)
    private String actorRole;

    @Column(columnDefinition = "text")
    private String metadataJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
