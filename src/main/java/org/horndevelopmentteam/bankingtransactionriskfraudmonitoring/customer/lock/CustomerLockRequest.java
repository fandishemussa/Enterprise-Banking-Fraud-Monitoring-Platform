package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import jakarta.persistence.Column;
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
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;

import java.time.LocalDateTime;

/**
 * One row per lock/unlock decision on a customer, tracking who asked and who (if anyone) reviewed
 * it. An ADMIN-initiated lock is recorded as an already-APPROVED row (requestedBy == reviewedBy,
 * reviewedAt == createdAt) so the history reads consistently regardless of which role triggered it,
 * even though only the INVESTIGATOR path actually waits for a separate approval step.
 */
@Entity
@Table(name = "customer_lock_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String lockRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @Column(nullable = false, updatable = false)
    private String requestedBy;

    @Column(nullable = false, updatable = false)
    private String requestedByRole;

    @Column(updatable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerLockRequestStatus status;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private String reviewNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
