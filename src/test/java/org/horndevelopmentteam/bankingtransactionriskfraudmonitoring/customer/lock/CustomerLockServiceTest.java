package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerLockServiceTest {

    private CustomerLockRequestRepository lockRequestRepository;
    private CustomerRepository customerRepository;
    private CustomerService customerService;
    private CustomerLockService customerLockService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        lockRequestRepository = mock(CustomerLockRequestRepository.class);
        customerRepository = mock(CustomerRepository.class);
        customerService = mock(CustomerService.class);
        IdSequenceService idSequenceService = mock(IdSequenceService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);

        customer = Customer.builder()
                .id(1L).customerId("CUS-1001").fullName("Jane Doe").email("jane@example.com")
                .country("US").riskLevel(CustomerRiskLevel.LOW).status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(customerService.findByPublicIdOrThrow("CUS-1001")).thenReturn(customer);
        when(idSequenceService.next("LOCK")).thenReturn("LOCK-1001");
        when(lockRequestRepository.save(any(CustomerLockRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerLockService = new CustomerLockService(
                lockRequestRepository, customerRepository, customerService, idSequenceService, auditLogService);
    }

    @Test
    void adminLockTakesEffectImmediately() {
        CustomerLockRequestResponse response = customerLockService.lock("CUS-1001", "confirmed fraud", "admin", "ADMIN");

        assertThat(response.status()).isEqualTo(CustomerLockRequestStatus.APPROVED);
        assertThat(response.reviewedBy()).isEqualTo("admin");
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.LOCKED);
    }

    @Test
    void investigatorLockOnlyCreatesPendingRequest() {
        CustomerLockRequestResponse response = customerLockService.lock(
                "CUS-1001", "suspicious activity", "investigator1", "INVESTIGATOR");

        assertThat(response.status()).isEqualTo(CustomerLockRequestStatus.PENDING);
        assertThat(response.reviewedBy()).isNull();
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void adminApprovingPendingRequestLocksCustomer() {
        CustomerLockRequest pending = CustomerLockRequest.builder()
                .lockRequestId("LOCK-1001").customer(customer).requestedBy("investigator1")
                .requestedByRole("INVESTIGATOR").reason("suspicious activity")
                .status(CustomerLockRequestStatus.PENDING).createdAt(LocalDateTime.now())
                .build();
        when(lockRequestRepository.findByLockRequestId("LOCK-1001")).thenReturn(Optional.of(pending));

        CustomerLockRequestResponse response = customerLockService.approve("LOCK-1001", "admin", "confirmed");

        assertThat(response.status()).isEqualTo(CustomerLockRequestStatus.APPROVED);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.LOCKED);
    }

    @Test
    void adminRejectingPendingRequestLeavesCustomerActive() {
        CustomerLockRequest pending = CustomerLockRequest.builder()
                .lockRequestId("LOCK-1001").customer(customer).requestedBy("investigator1")
                .requestedByRole("INVESTIGATOR").reason("suspicious activity")
                .status(CustomerLockRequestStatus.PENDING).createdAt(LocalDateTime.now())
                .build();
        when(lockRequestRepository.findByLockRequestId("LOCK-1001")).thenReturn(Optional.of(pending));

        CustomerLockRequestResponse response = customerLockService.reject("LOCK-1001", "admin", "insufficient evidence");

        assertThat(response.status()).isEqualTo(CustomerLockRequestStatus.REJECTED);
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void cannotLockAnAlreadyLockedCustomer() {
        customer.setStatus(CustomerStatus.LOCKED);

        assertThatThrownBy(() -> customerLockService.lock("CUS-1001", "reason", "admin", "ADMIN"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotSubmitADuplicatePendingRequest() {
        when(lockRequestRepository.existsByCustomerAndStatus(customer, CustomerLockRequestStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> customerLockService.lock("CUS-1001", "reason", "investigator1", "INVESTIGATOR"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void unlockSetsCustomerBackToActive() {
        customer.setStatus(CustomerStatus.LOCKED);

        customerLockService.unlock("CUS-1001", "admin");

        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void unlockingANonLockedCustomerThrows() {
        assertThatThrownBy(() -> customerLockService.unlock("CUS-1001", "admin"))
                .isInstanceOf(BadRequestException.class);
    }
}
