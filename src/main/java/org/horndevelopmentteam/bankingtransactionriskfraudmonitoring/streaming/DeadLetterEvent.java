package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

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

/** Distinct from ingestion.DeadLetterTransaction (that one is for the CSV batch-ingestion
 * pipeline) - this is specifically for events that failed streaming consumption. */
@Entity
@Table(name = "streaming_dead_letter_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private String sourceTopic;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false, updatable = false)
    private String errorReason;

    @Column(nullable = false, updatable = false)
    private String errorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadLetterEventStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
