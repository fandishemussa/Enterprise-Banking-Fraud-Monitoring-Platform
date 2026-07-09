package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Rows that could not even be parsed into a {@link StagingTransaction} (malformed structure,
 * wrong column count, unexpected exception) - distinct from {@link RejectedTransaction}, which
 * holds well-formed rows that simply failed business validation.
 */
@Entity
@Table(name = "dead_letter_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_run_id")
    private IngestionRun ingestionRun;

    @Lob
    @Column(nullable = false)
    private String rawPayload;

    @Column(nullable = false)
    private String errorType;

    @Lob
    @Column(nullable = false)
    private String errorReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeadLetterStatus processedStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime lastRetryAt;
}
