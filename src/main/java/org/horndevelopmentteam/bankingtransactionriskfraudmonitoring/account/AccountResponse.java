package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String accountId,
        String customerId,
        AccountType accountType,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountId(),
                account.getCustomer().getCustomerId(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
