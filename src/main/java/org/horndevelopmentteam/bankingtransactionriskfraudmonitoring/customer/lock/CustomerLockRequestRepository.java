package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerLockRequestRepository extends JpaRepository<CustomerLockRequest, Long> {

    Optional<CustomerLockRequest> findByLockRequestId(String lockRequestId);

    List<CustomerLockRequest> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<CustomerLockRequest> findByStatusOrderByCreatedAtDesc(CustomerLockRequestStatus status);

    boolean existsByCustomerAndStatus(Customer customer, CustomerLockRequestStatus status);
}
