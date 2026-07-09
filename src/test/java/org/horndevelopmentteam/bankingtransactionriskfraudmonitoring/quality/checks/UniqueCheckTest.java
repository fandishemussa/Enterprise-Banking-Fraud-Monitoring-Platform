package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UniqueCheckTest {

    private final UniqueCheck check = new UniqueCheck();

    private CleanTransaction transaction(String id) {
        return CleanTransaction.builder()
                .transactionId(id)
                .customerId("CUS-1")
                .sourceAccountId("ACC-1")
                .amount(BigDecimal.TEN)
                .currency("USD")
                .transactionType(TransactionType.PAYMENT)
                .channel(TransactionChannel.WEB)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now().minusDays(1))
                .loadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void flagsDuplicateTransactionIds() {
        QualityCheckOutcome outcome = check.run(List.of(
                transaction("TXN-1"),
                transaction("TXN-1"),
                transaction("TXN-2")
        ));

        assertThat(outcome.recordsFailed()).isEqualTo(2);
        assertThat(outcome.issues()).hasSize(2)
                .allMatch(issue -> issue.recordIdentifier().equals("TXN-1"));
    }

    @Test
    void passesWhenAllUnique() {
        QualityCheckOutcome outcome = check.run(List.of(transaction("TXN-1"), transaction("TXN-2")));

        assertThat(outcome.passed()).isTrue();
    }
}
