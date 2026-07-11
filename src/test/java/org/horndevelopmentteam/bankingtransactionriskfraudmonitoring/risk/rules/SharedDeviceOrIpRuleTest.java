package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionChannel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedDeviceOrIpRuleTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final FraudRuleConfigService fraudRuleConfigService = mock(FraudRuleConfigService.class);
    private final SharedDeviceOrIpRule rule = new SharedDeviceOrIpRule(transactionRepository, fraudRuleConfigService);

    {
        when(fraudRuleConfigService.isExplicitlyDisabled("SHARED_DEVICE_IP_RULE")).thenReturn(false);
        when(fraudRuleConfigService.findEnabledConfig("SHARED_DEVICE_IP_RULE")).thenReturn(Optional.empty());
    }

    private Customer customer(String id) {
        return Customer.builder()
                .id(1L).customerId(id).fullName("Test Customer").email(id + "@example.com")
                .country("US").riskLevel(CustomerRiskLevel.LOW).status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    private BankingTransaction transaction(String deviceId, String ipAddress) {
        return BankingTransaction.builder()
                .transactionId("TXN-1").customer(customer("CUS-1001"))
                .amount(BigDecimal.valueOf(100)).currency("USD")
                .transactionType(TransactionType.PAYMENT).channel(TransactionChannel.WEB)
                .country("US").status(TransactionStatus.SUCCESS).createdAt(LocalDateTime.now())
                .deviceId(deviceId).ipAddress(ipAddress)
                .build();
    }

    @Test
    void doesNotTriggerWhenNoOverlap() {
        when(transactionRepository.existsByDeviceIdAndCustomerNot(anyString(), any())).thenReturn(false);
        when(transactionRepository.existsByIpAddressAndCustomerNot(anyString(), any())).thenReturn(false);

        RuleResult result = rule.evaluate(transaction("device-1", "1.2.3.4"));

        assertThat(result.triggered()).isFalse();
    }

    @Test
    void triggersWhenDeviceSharedWithAnotherCustomer() {
        when(transactionRepository.existsByDeviceIdAndCustomerNot("device-1", null)).thenReturn(false);
        BankingTransaction txn = transaction("device-1", "1.2.3.4");
        when(transactionRepository.existsByDeviceIdAndCustomerNot("device-1", txn.getCustomer())).thenReturn(true);
        when(transactionRepository.existsByIpAddressAndCustomerNot("1.2.3.4", txn.getCustomer())).thenReturn(false);

        RuleResult result = rule.evaluate(txn);

        assertThat(result.triggered()).isTrue();
        assertThat(result.points()).isEqualTo(30);
    }

    @Test
    void triggersWhenIpSharedWithAnotherCustomer() {
        BankingTransaction txn = transaction("device-1", "1.2.3.4");
        when(transactionRepository.existsByDeviceIdAndCustomerNot("device-1", txn.getCustomer())).thenReturn(false);
        when(transactionRepository.existsByIpAddressAndCustomerNot("1.2.3.4", txn.getCustomer())).thenReturn(true);

        RuleResult result = rule.evaluate(txn);

        assertThat(result.triggered()).isTrue();
    }
}
