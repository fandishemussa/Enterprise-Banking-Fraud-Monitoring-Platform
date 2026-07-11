package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LargeAmountRuleTest {

    private final FraudRuleConfigService fraudRuleConfigService = mock(FraudRuleConfigService.class);
    private final LargeAmountRule rule = new LargeAmountRule(fraudRuleConfigService);

    {
        when(fraudRuleConfigService.isExplicitlyDisabled("LARGE_AMOUNT_RULE")).thenReturn(false);
        when(fraudRuleConfigService.findEnabledConfig("LARGE_AMOUNT_RULE")).thenReturn(Optional.empty());
    }

    private BankingTransaction transactionOf(BigDecimal amount) {
        return BankingTransaction.builder()
                .transactionId("TXN-1")
                .amount(amount)
                .currency("USD")
                .transactionType(TransactionType.PAYMENT)
                .channel(TransactionChannel.WEB)
                .country("US")
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void doesNotTriggerForNormalAmount() {
        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(100)));

        assertThat(result.triggered()).isFalse();
        assertThat(result.points()).isZero();
    }

    @Test
    void triggersModeratelyAboveFiveThousand() {
        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(6000)));

        assertThat(result.triggered()).isTrue();
        assertThat(result.points()).isEqualTo(30);
    }

    @Test
    void triggersHeavilyAboveTenThousand() {
        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(15000)));

        assertThat(result.triggered()).isTrue();
        assertThat(result.points()).isEqualTo(50);
    }
}
