package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.AccountRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionCsvValidator {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CleanTransactionRepository cleanTransactionRepository;

    public record ValidationResult(RejectionReason reason, String detail) {
    }

    /**
     * Returns the first rule the row fails, or null if the row is valid and can be promoted
     * to clean_transactions.
     */
    public ValidationResult validate(StagingTransaction staging) {
        if (isBlank(staging.getTransactionId()) || isBlank(staging.getCustomerId())
                || isBlank(staging.getSourceAccountId()) || isBlank(staging.getCurrency())
                || isBlank(staging.getTransactionType()) || isBlank(staging.getChannel())
                || isBlank(staging.getStatus()) || staging.getAmount() == null || staging.getCreatedAt() == null) {
            return new ValidationResult(RejectionReason.MISSING_REQUIRED_FIELD,
                    "One or more required fields are missing or could not be parsed");
        }

        if (staging.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ValidationResult(RejectionReason.INVALID_AMOUNT,
                    "Amount must be greater than 0, was " + staging.getAmount());
        }

        if (!isValidEnum(TransactionType.class, staging.getTransactionType())) {
            return new ValidationResult(RejectionReason.INVALID_TRANSACTION_TYPE,
                    "Unrecognized transactionType: " + staging.getTransactionType());
        }

        if (!isValidEnum(TransactionChannel.class, staging.getChannel())) {
            return new ValidationResult(RejectionReason.INVALID_CHANNEL,
                    "Unrecognized channel: " + staging.getChannel());
        }

        if (!isValidEnum(TransactionStatus.class, staging.getStatus())) {
            return new ValidationResult(RejectionReason.INVALID_STATUS,
                    "Unrecognized status: " + staging.getStatus());
        }

        if (staging.getCreatedAt().isAfter(LocalDateTime.now())) {
            return new ValidationResult(RejectionReason.FUTURE_CREATED_AT,
                    "createdAt is in the future: " + staging.getCreatedAt());
        }

        if (!customerRepository.existsByCustomerId(staging.getCustomerId())) {
            return new ValidationResult(RejectionReason.UNKNOWN_CUSTOMER,
                    "No customer found with id " + staging.getCustomerId());
        }

        if (accountRepository.findByAccountId(staging.getSourceAccountId()).isEmpty()) {
            return new ValidationResult(RejectionReason.UNKNOWN_ACCOUNT,
                    "No account found with id " + staging.getSourceAccountId());
        }

        if (staging.getDestinationAccountId() != null
                && accountRepository.findByAccountId(staging.getDestinationAccountId()).isEmpty()) {
            return new ValidationResult(RejectionReason.UNKNOWN_ACCOUNT,
                    "No destination account found with id " + staging.getDestinationAccountId());
        }

        if (cleanTransactionRepository.existsByTransactionId(staging.getTransactionId())) {
            return new ValidationResult(RejectionReason.DUPLICATE_TRANSACTION_ID,
                    "transactionId " + staging.getTransactionId() + " already loaded");
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String value) {
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
