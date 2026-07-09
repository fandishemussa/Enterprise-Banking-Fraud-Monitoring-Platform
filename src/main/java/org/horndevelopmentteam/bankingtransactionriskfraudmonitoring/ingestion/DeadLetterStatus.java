package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

public enum DeadLetterStatus {
    NEW,
    RETRIED,
    IGNORED,
    RESOLVED
}
