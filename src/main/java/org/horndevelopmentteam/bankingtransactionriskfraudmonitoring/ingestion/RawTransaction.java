package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Untouched landing copy of a single CSV row (every field kept as raw string),
 * so the exact source data can always be replayed regardless of downstream parsing changes.
 */
@Entity
@Table(name = "raw_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_run_id", nullable = false)
    private IngestionRun ingestionRun;

    @Column(nullable = false)
    private int rowNumber;

    @Lob
    @Column(nullable = false)
    private String rawLine;

    private String transactionIdRaw;
    private String customerIdRaw;
    private String sourceAccountIdRaw;
    private String destinationAccountIdRaw;
    private String amountRaw;
    private String currencyRaw;
    private String transactionTypeRaw;
    private String channelRaw;
    private String merchantCategoryRaw;
    private String countryRaw;
    private String deviceIdRaw;
    private String ipAddressRaw;
    private String statusRaw;
    private String createdAtRaw;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ingestedAt;
}
