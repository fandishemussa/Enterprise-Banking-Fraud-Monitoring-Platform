package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto.CustomerResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = Customer.builder()
                .customerId(idSequenceService.next("CUS"))
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .country(request.country())
                .riskLevel(CustomerRiskLevel.LOW)
                .status(CustomerStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Customer saved = customerRepository.save(customer);

        auditLogService.record(
                AuditEventType.CUSTOMER_CREATED,
                "Customer",
                saved.getCustomerId(),
                null,
                saved.getFullName(),
                "Customer " + saved.getCustomerId() + " created"
        );

        return CustomerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPublicId(String customerId) {
        return CustomerResponse.from(findByPublicIdOrThrow(customerId));
    }

    @Transactional
    public CustomerResponse updateCustomer(String customerId, CustomerRequest request) {
        Customer customer = findByPublicIdOrThrow(customerId);
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setCountry(request.country());
        customer.setUpdatedAt(LocalDateTime.now());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public Customer findByPublicIdOrThrow(String customerId) {
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }
}
