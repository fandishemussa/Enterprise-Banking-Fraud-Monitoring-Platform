package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Isolated from IdempotencyService so a failed insert's DataIntegrityViolationException propagates
 * out of a *separate* Spring-proxied transaction boundary - see IdempotencyService's class comment
 * for why that separation is required (reusing the same Hibernate session after a caught constraint
 * violation corrupts it).
 */
@Service
@RequiredArgsConstructor
class IdempotencyClaimWriter {

    private final IdempotencyKeyRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertClaim(String idempotencyKey, String endpoint) {
        repository.save(IdempotencyKeyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .endpoint(endpoint)
                .resourceId(null)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
