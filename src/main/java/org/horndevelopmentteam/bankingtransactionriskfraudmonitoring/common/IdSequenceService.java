package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdSequenceService {

    private static final long INITIAL_VALUE = 1000L;

    private final IdSequenceRepository idSequenceRepository;

    /**
     * Atomically increments and returns the next public ID for the given prefix,
     * e.g. next("CUS") -> "CUS-1001". Runs in its own transaction so the row lock
     * is released immediately regardless of the caller's transaction outcome.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String next(String prefix) {
        IdSequence sequence = idSequenceRepository.findWithLockByPrefix(prefix)
                .orElseGet(() -> new IdSequence(prefix, INITIAL_VALUE));
        long nextValue = sequence.getLastValue() + 1;
        sequence.setLastValue(nextValue);
        idSequenceRepository.save(sequence);
        return prefix + "-" + nextValue;
    }
}
