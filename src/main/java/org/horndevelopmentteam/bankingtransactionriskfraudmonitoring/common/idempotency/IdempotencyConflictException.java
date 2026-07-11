package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.idempotency;

/** Thrown when a request reuses an Idempotency-Key that's still being processed by another
 * in-flight request (as opposed to one that already finished, which is replayed instead). */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
