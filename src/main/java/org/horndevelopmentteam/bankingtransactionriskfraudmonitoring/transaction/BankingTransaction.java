package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.Account;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto.DeterministicEncryptedStringConverter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto.EncryptedStringConverter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "banking_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionChannel channel;

    private String merchantCategory;

    @Column(nullable = false)
    private String country;

    // Deterministic encryption - looked up by exact value
    // (TransactionRepository.existsByCustomerAndDeviceIdAndIdNot, used by NewDeviceRule).
    @Convert(converter = DeterministicEncryptedStringConverter.class)
    @Column(length = 500)
    private String deviceId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 500)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
