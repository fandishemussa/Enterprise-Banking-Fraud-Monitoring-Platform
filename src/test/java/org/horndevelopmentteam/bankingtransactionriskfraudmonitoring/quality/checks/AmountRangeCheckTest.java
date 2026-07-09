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

class AmountRangeCheckTest {

    private final AmountRangeCheck check = new AmountRangeCheck();

    private CleanTransaction transaction(String id, BigDecimal amount) {
        return CleanTransaction.builder()
                .transactionId(id)
                .customerId("CUS-1")
                .sourceAccountId("ACC-1")
                .amount(amount)
                .currency("USD")
                .transactionType(TransactionType.PAYMENT)
                .channel(TransactionChannel.WEB)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now().minusDays(1))
                .loadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void flagsZeroAndNegativeAmounts() {
        QualityCheckOutcome outcome = check.run(List.of(
                transaction("TXN-1", BigDecimal.valueOf(100)),
                transaction("TXN-2", BigDecimal.ZERO),
                transaction("TXN-3", BigDecimal.valueOf(-5))
        ));

        assertThat(outcome.recordsChecked()).isEqualTo(3);
        assertThat(outcome.recordsFailed()).isEqualTo(2);
        assertThat(outcome.issues()).extracting(QualityCheckOutcome.Issue::recordIdentifier)
                .containsExactlyInAnyOrder("TXN-2", "TXN-3");
    }

    @Test
    void passesWhenAllAmountsPositive() {
        QualityCheckOutcome outcome = check.run(List.of(transaction("TXN-1", BigDecimal.TEN)));

        assertThat(outcome.passed()).isTrue();
    }
}
