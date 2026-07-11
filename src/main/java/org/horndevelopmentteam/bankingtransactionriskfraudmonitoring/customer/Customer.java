package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto.DeterministicEncryptedStringConverter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.crypto.EncryptedStringConverter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String customerId;

    @Column(nullable = false)
    private String fullName;

    // Deterministic (not random-IV) encryption: this column has a UNIQUE constraint and is looked
    // up by exact value (CustomerRepository.existsByEmail), which random-IV AES-GCM would break.
    @Convert(converter = DeterministicEncryptedStringConverter.class)
    @Column(unique = true, nullable = false, length = 500)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 500)
    private String phone;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerRiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
