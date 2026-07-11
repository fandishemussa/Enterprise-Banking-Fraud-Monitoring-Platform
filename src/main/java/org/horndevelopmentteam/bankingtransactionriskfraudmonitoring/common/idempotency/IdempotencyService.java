package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implements the standard "claim, then complete" idempotency-key protocol (the same shape Stripe's
 * API uses): the first request for a given key inserts a placeholder row (resourceId still null)
 * as a claim, protected by a unique (idempotency_key, endpoint) constraint so a concurrent request
 * with the same key can't also claim it. Once the original request finishes, it fills in the
 * resourceId. A retried request for a key that already completed gets the original resourceId back
 * instead of re-running the operation; a retried request for a key that's still mid-flight gets a
 * 409 rather than silently creating a second copy.
 *
 * The insert attempt and the fallback lookup deliberately run in two *separate* REQUIRES_NEW
 * transactions via {@link IdempotencyClaimWriter}, rather than one transaction with an internal
 * try/catch around the insert. A caught DataIntegrityViolationException from a failed insert still
 * leaves the Hibernate session for that transaction in a state where any further use throws
 * `HHH000099: an assertion failure occurred ... has a null identifier (this can happen if the
 * session is flushed after an exception occurs)` - confirmed by hitting exactly that in a live
 * concurrent-key test. Only a fresh transaction (fresh session) can safely run the fallback query.
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private final IdempotencyClaimWriter claimWriter;

    /** Returns the existing resourceId if this key already completed for this endpoint. Returns
     * empty if this call successfully claimed the key (caller should proceed and call complete()).
     * Throws IdempotencyConflictException if another request currently holds this key. */
    public Optional<String> claim(String idempotencyKey, String endpoint) {
        try {
            claimWriter.insertClaim(idempotencyKey, endpoint);
            return Optional.empty();
        } catch (DataIntegrityViolationException ex) {
            IdempotencyKeyRecord existing = findExisting(idempotencyKey, endpoint)
                    .orElseThrow(() -> ex);
            if (existing.getResourceId() == null) {
                throw new IdempotencyConflictException(
                        "A request with this Idempotency-Key is already being processed");
            }
            return Optional.of(existing.getResourceId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<IdempotencyKeyRecord> findExisting(String idempotencyKey, String endpoint) {
        return repository.findByIdempotencyKeyAndEndpoint(idempotencyKey, endpoint);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String idempotencyKey, String endpoint, String resourceId) {
        repository.findByIdempotencyKeyAndEndpoint(idempotencyKey, endpoint)
                .ifPresent(record -> {
                    record.setResourceId(resourceId);
                    repository.save(record);
                });
    }
}
