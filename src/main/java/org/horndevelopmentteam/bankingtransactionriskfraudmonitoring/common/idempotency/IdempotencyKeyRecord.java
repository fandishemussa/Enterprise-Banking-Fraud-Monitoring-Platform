package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row per (idempotencyKey, endpoint) pair. resourceId is null while the original request is
 * still being processed (acts as a claim/lock via the unique constraint below) and gets filled in
 * once that request finishes - see IdempotencyService for the full protocol.
 */
@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(columnNames = {"idempotency_key", "endpoint"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKeyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String endpoint;

    private String resourceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
