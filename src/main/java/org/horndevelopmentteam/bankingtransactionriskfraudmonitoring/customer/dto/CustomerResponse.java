package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerResponse(
        String customerId,
        String fullName,
        String email,
        String phone,
        String country,
        CustomerRiskLevel riskLevel,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCountry(),
                customer.getRiskLevel(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
