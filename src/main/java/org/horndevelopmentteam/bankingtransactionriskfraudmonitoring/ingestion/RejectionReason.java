package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

public enum RejectionReason {
    MISSING_REQUIRED_FIELD,
    INVALID_AMOUNT,
    INVALID_TRANSACTION_TYPE,
    INVALID_CHANNEL,
    INVALID_STATUS,
    UNKNOWN_CUSTOMER,
    UNKNOWN_ACCOUNT,
    DUPLICATE_TRANSACTION_ID,
    FUTURE_CREATED_AT,
    OTHER
}
