package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.dto;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;

public record CustomerRiskProfileResponse(
        String customerId,
        String fullName,
        CustomerRiskLevel riskLevel,
        CustomerStatus status,
        long totalTransactions,
        long totalOpenAlerts,
        long totalConfirmedFraudCases
) {
}
