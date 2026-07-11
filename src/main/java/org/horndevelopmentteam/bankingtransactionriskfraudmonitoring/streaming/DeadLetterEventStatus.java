package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

public enum DeadLetterEventStatus {
    NEW,
    RETRIED,
    IGNORED,
    RESOLVED
}
