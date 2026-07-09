package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.account;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountId(String accountId);

    List<Account> findByCustomer(Customer customer);
}
