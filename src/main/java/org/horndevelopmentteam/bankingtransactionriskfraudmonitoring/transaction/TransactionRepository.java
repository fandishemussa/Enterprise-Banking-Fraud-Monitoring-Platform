package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account.Account;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<BankingTransaction, Long> {

    Optional<BankingTransaction> findByTransactionId(String transactionId);

    List<BankingTransaction> findByCustomer(Customer customer);

    List<BankingTransaction> findBySourceAccount(Account account);

    boolean existsByCustomerAndDeviceIdAndIdNot(Customer customer, String deviceId, Long id);

    boolean existsByCustomerAndCountryAndIdNot(Customer customer, String country, Long id);

    long countByCustomerAndCreatedAtAfter(Customer customer, LocalDateTime after);

    boolean existsByDeviceIdAndCustomerNot(String deviceId, Customer customer);

    boolean existsByIpAddressAndCustomerNot(String ipAddress, Customer customer);

    List<BankingTransaction> findByDeviceIdIn(List<String> deviceIds);

    List<BankingTransaction> findByIpAddressIn(List<String> ipAddresses);
}
