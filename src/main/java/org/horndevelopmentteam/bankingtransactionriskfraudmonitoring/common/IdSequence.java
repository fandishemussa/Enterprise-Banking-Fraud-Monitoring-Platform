package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Backs {@link IdSequenceService}. One row per public-ID prefix (CUS, ACC, TXN, ALERT, CASE),
 * incremented under a row lock so public IDs stay unique across restarts and instances.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdSequence {

    @Id
    private String prefix;

    private long lastValue;
}
