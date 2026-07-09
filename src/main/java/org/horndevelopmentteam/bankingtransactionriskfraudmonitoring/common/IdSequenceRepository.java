package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<IdSequence> findWithLockByPrefix(String prefix);
}
