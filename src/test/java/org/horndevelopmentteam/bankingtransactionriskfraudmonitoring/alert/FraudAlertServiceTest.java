package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla.AlertSlaService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.ScoringSource;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudAlertServiceTest {

    private FraudAlertRepository fraudAlertRepository;
    private FraudAlertService fraudAlertService;

    private final Customer customer = Customer.builder()
            .id(1L).customerId("CUS-1001").fullName("Jane Doe").email("jane@example.com")
            .country("US").riskLevel(CustomerRiskLevel.LOW).status(CustomerStatus.ACTIVE)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .build();

    @BeforeEach
    void setUp() {
        fraudAlertRepository = mock(FraudAlertRepository.class);
        IdSequenceService idSequenceService = mock(IdSequenceService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        AlertSlaService alertSlaService = mock(AlertSlaService.class);

        when(idSequenceService.next("ALERT")).thenReturn("ALERT-1001");
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        fraudAlertService = new FraudAlertService(fraudAlertRepository, idSequenceService, auditLogService, meterRegistry, alertSlaService);
    }

    private BankingTransaction transaction() {
        return BankingTransaction.builder()
                .id(1L).transactionId("TXN-1001").customer(customer)
                .amount(java.math.BigDecimal.valueOf(100)).currency("USD")
                .transactionType(TransactionType.PAYMENT).channel(TransactionChannel.WEB)
                .country("US").status(TransactionStatus.SUCCESS).createdAt(LocalDateTime.now())
                .build();
    }

    private RiskScore riskScore(RiskLevel level, int score) {
        return RiskScore.builder()
                .transaction(transaction()).ruleScore(score).finalScore(score).riskLevel(level)
                .explanation("test").scoringSource(ScoringSource.RULE_ONLY).createdAt(LocalDateTime.now())
                .build();
    }

    @ParameterizedTest
    @EnumSource(value = RiskLevel.class, names = {"LOW", "MEDIUM"})
    void doesNotCreateAlertForLowOrMediumRisk(RiskLevel level) {
        FraudAlert alert = fraudAlertService.createAlertIfWarranted(transaction(), riskScore(level, 40));

        assertThat(alert).isNull();
    }

    @Test
    void createsHighPriorityAlertForHighRisk() {
        FraudAlert alert = fraudAlertService.createAlertIfWarranted(transaction(), riskScore(RiskLevel.HIGH, 70));

        assertThat(alert).isNotNull();
        assertThat(alert.getPriority()).isEqualTo(AlertPriority.HIGH);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getAlertId()).isEqualTo("ALERT-1001");
    }

    @Test
    void createsCriticalPriorityAlertForCriticalRisk() {
        FraudAlert alert = fraudAlertService.createAlertIfWarranted(transaction(), riskScore(RiskLevel.CRITICAL, 95));

        assertThat(alert).isNotNull();
        assertThat(alert.getPriority()).isEqualTo(AlertPriority.CRITICAL);
    }
}
