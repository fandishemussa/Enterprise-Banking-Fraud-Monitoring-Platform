package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.CaseResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerRiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.enums.CustomerStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerRiskProfileResponse(
        String customerId,
        String fullName,
        String email,
        String phone,
        String country,
        CustomerRiskLevel riskLevel,
        CustomerStatus status,
        LocalDateTime createdAt,

        long totalTransactions,
        BigDecimal totalTransactionAmount,
        BigDecimal averageTransactionAmount,
        BigDecimal maxTransactionAmount,
        long totalAlerts,
        long openAlerts,
        long confirmedFraudCount,
        long falsePositiveCount,
        long totalCases,
        long openCases,
        long criticalTransactionCount,
        long highRiskTransactionCount,

        CustomerBehaviorSummary behavior,

        List<TransactionResponse> recentTransactions,
        List<RiskScoreResponse> recentRiskScores,
        List<FraudAlertResponse> recentAlerts,
        List<CaseResponse> recentCases,

        List<CustomerRiskTrendPoint> riskTrendData,
        List<CustomerTransactionTrendPoint> transactionVolumeTrend,
        List<CustomerTransactionTrendPoint> alertTrend
) {
}
