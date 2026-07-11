package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomAmountThresholdRuleTest {

    private final FraudRuleRepository fraudRuleRepository = mock(FraudRuleRepository.class);
    private final CustomAmountThresholdRule rule = new CustomAmountThresholdRule(fraudRuleRepository);

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

    private FraudRule customRule(String code, String threshold, String secondaryThreshold, int score, Integer secondaryScore, RiskLevel severity) {
        return FraudRule.builder()
                .ruleId("RULE-CUSTOM")
                .ruleCode(code)
                .name("Custom Rule " + code)
                .enabled(true)
                .thresholdValue(threshold)
                .secondaryThresholdValue(secondaryThreshold)
                .scoreImpact(score)
                .secondaryScoreImpact(secondaryScore)
                .severity(severity)
                .ruleType(FraudRuleType.CUSTOM)
                .build();
    }

    @Test
    void doesNotTriggerWhenNoCustomRulesConfigured() {
        when(fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM)).thenReturn(List.of());

        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(100_000)));

        assertThat(result.triggered()).isFalse();
    }

    @Test
    void doesNotTriggerBelowThreshold() {
        when(fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM))
                .thenReturn(List.of(customRule("CUSTOM_1", "2000", null, 40, null, RiskLevel.HIGH)));

        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(500)));

        assertThat(result.triggered()).isFalse();
    }

    @Test
    void triggersAboveThreshold() {
        when(fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM))
                .thenReturn(List.of(customRule("CUSTOM_1", "2000", null, 40, null, RiskLevel.HIGH)));

        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(3000)));

        assertThat(result.triggered()).isTrue();
        assertThat(result.points()).isEqualTo(40);
        assertThat(result.severity()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    void usesSecondaryScoreAboveSecondaryThreshold() {
        when(fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM))
                .thenReturn(List.of(customRule("CUSTOM_1", "2000", "8000", 40, 70, RiskLevel.HIGH)));

        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(9000)));

        assertThat(result.points()).isEqualTo(70);
    }

    @Test
    void sumsPointsAcrossMultipleTriggeredCustomRules() {
        when(fraudRuleRepository.findByRuleTypeAndEnabledTrue(FraudRuleType.CUSTOM))
                .thenReturn(List.of(
                        customRule("CUSTOM_1", "1000", null, 20, null, RiskLevel.MEDIUM),
                        customRule("CUSTOM_2", "2000", null, 30, null, RiskLevel.CRITICAL)));

        RuleResult result = rule.evaluate(transactionOf(BigDecimal.valueOf(5000)));

        assertThat(result.points()).isEqualTo(50);
        assertThat(result.severity()).isEqualTo(RiskLevel.CRITICAL);
    }
}
