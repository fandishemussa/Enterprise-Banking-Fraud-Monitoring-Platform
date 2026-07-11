package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums;

public enum CustomerStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED,
    /** Set only via CustomerLockService - blocks new transactions (see TransactionService) while a
     * fraud investigation is ongoing. Distinct from SUSPENDED, which is a general administrative
     * status unrelated to the lock/approval workflow. */
    LOCKED
}
