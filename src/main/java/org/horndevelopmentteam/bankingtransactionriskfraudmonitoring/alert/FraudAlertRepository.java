package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    Optional<FraudAlert> findByAlertId(String alertId);

    Optional<FraudAlert> findByTransaction_TransactionId(String transactionId);

    List<FraudAlert> findByCustomer(Customer customer);

    long countByCustomerAndStatus(Customer customer, AlertStatus status);

    Page<FraudAlert> findByAssignedTo(String assignedTo, Pageable pageable);
}
