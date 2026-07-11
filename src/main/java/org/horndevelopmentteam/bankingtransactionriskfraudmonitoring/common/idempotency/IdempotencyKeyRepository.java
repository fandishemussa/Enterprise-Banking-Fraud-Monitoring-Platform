package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyRecord, Long> {
    Optional<IdempotencyKeyRecord> findByIdempotencyKeyAndEndpoint(String idempotencyKey, String endpoint);
}
