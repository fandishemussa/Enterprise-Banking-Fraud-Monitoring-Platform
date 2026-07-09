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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Type-coerced view of a raw row (numbers/dates parsed where possible). Business validation
 * happens one step later, against these already-typed fields, in {@link TransactionCsvValidator}.
 */
@Entity
@Table(name = "staging_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StagingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_run_id", nullable = false)
    private IngestionRun ingestionRun;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_transaction_id", nullable = false, unique = true)
    private RawTransaction rawTransaction;

    private String transactionId;
    private String customerId;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String channel;
    private String merchantCategory;
    private String country;
    private String deviceId;
    private String ipAddress;
    private String status;
    private LocalDateTime createdAt;

    @Lob
    private String parseErrors;

    @Column(nullable = false, updatable = false)
    private LocalDateTime stagedAt;
}
