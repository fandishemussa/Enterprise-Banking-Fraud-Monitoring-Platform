package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.BadRequestException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lock/unlock workflow for customers under fraud investigation (see TransactionService, which
 * rejects new transactions for a LOCKED customer). ADMIN locks take effect immediately; INVESTIGATOR
 * locks only create a PENDING request - the customer stays ACTIVE until an ADMIN approves it. Both
 * paths funnel through CustomerLockRequest so the review history reads the same either way.
 */
@Service
@RequiredArgsConstructor
public class CustomerLockService {

    private final CustomerLockRequestRepository lockRequestRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerLockRequestResponse lock(String customerId, String reason, String actorUsername, String actorRole) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);

        if (customer.getStatus() == CustomerStatus.LOCKED) {
            throw new BadRequestException("Customer " + customerId + " is already locked");
        }
        if (lockRequestRepository.existsByCustomerAndStatus(customer, CustomerLockRequestStatus.PENDING)) {
            throw new BadRequestException("Customer " + customerId + " already has a pending lock request");
        }

        boolean isAdmin = "ADMIN".equals(actorRole);
        LocalDateTime now = LocalDateTime.now();

        CustomerLockRequest.CustomerLockRequestBuilder builder = CustomerLockRequest.builder()
                .lockRequestId(idSequenceService.next("LOCK"))
                .customer(customer)
                .requestedBy(actorUsername)
                .requestedByRole(actorRole)
                .reason(reason)
                .createdAt(now);

        if (isAdmin) {
            builder.status(CustomerLockRequestStatus.APPROVED)
                    .reviewedBy(actorUsername)
                    .reviewedAt(now);
        } else {
            builder.status(CustomerLockRequestStatus.PENDING);
        }

        CustomerLockRequest saved = lockRequestRepository.save(builder.build());

        if (isAdmin) {
            applyLock(customer);
            auditLogService.record(AuditEventType.CUSTOMER_LOCKED, "Customer", customerId,
                    CustomerStatus.ACTIVE.name(), CustomerStatus.LOCKED.name(),
                    "Customer " + customerId + " locked directly by admin " + actorUsername + ": " + reason);
        } else {
            auditLogService.record(AuditEventType.CUSTOMER_LOCK_REQUESTED, "Customer", customerId,
                    null, saved.getLockRequestId(),
                    "Lock requested for customer " + customerId + " by " + actorUsername + " (" + actorRole + "): " + reason);
        }

        return CustomerLockRequestResponse.from(saved);
    }

    @Transactional
    public CustomerLockRequestResponse approve(String lockRequestId, String adminUsername, String notes) {
        CustomerLockRequest request = findRequestOrThrow(lockRequestId);
        if (request.getStatus() != CustomerLockRequestStatus.PENDING) {
            throw new BadRequestException("Lock request " + lockRequestId + " is not pending");
        }

        Customer customer = request.getCustomer();
        request.setStatus(CustomerLockRequestStatus.APPROVED);
        request.setReviewedBy(adminUsername);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(notes);
        CustomerLockRequest saved = lockRequestRepository.save(request);

        applyLock(customer);

        auditLogService.record(AuditEventType.CUSTOMER_LOCK_APPROVED, "Customer", customer.getCustomerId(),
                CustomerStatus.ACTIVE.name(), CustomerStatus.LOCKED.name(),
                "Lock request " + lockRequestId + " for customer " + customer.getCustomerId()
                        + " approved by " + adminUsername);

        return CustomerLockRequestResponse.from(saved);
    }

    @Transactional
    public CustomerLockRequestResponse reject(String lockRequestId, String adminUsername, String notes) {
        CustomerLockRequest request = findRequestOrThrow(lockRequestId);
        if (request.getStatus() != CustomerLockRequestStatus.PENDING) {
            throw new BadRequestException("Lock request " + lockRequestId + " is not pending");
        }

        request.setStatus(CustomerLockRequestStatus.REJECTED);
        request.setReviewedBy(adminUsername);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(notes);
        CustomerLockRequest saved = lockRequestRepository.save(request);

        auditLogService.record(AuditEventType.CUSTOMER_LOCK_REJECTED, "Customer", request.getCustomer().getCustomerId(),
                null, null,
                "Lock request " + lockRequestId + " for customer " + request.getCustomer().getCustomerId()
                        + " rejected by " + adminUsername);

        return CustomerLockRequestResponse.from(saved);
    }

    @Transactional
    public void unlock(String customerId, String adminUsername) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        if (customer.getStatus() != CustomerStatus.LOCKED) {
            throw new BadRequestException("Customer " + customerId + " is not locked");
        }
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        auditLogService.record(AuditEventType.CUSTOMER_UNLOCKED, "Customer", customerId,
                CustomerStatus.LOCKED.name(), CustomerStatus.ACTIVE.name(),
                "Customer " + customerId + " unlocked by admin " + adminUsername);
    }

    @Transactional(readOnly = true)
    public List<CustomerLockRequestResponse> getRequestsForCustomer(String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);
        return lockRequestRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(CustomerLockRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerLockRequestResponse> getPendingRequests() {
        return lockRequestRepository.findByStatusOrderByCreatedAtDesc(CustomerLockRequestStatus.PENDING).stream()
                .map(CustomerLockRequestResponse::from)
                .toList();
    }

    private void applyLock(Customer customer) {
        customer.setStatus(CustomerStatus.LOCKED);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private CustomerLockRequest findRequestOrThrow(String lockRequestId) {
        return lockRequestRepository.findByLockRequestId(lockRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Lock request not found: " + lockRequestId));
    }
}
