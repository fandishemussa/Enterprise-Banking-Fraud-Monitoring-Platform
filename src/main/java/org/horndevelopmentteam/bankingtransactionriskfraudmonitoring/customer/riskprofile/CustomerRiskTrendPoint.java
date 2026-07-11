package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile;

import java.time.LocalDate;

public record CustomerRiskTrendPoint(LocalDate date, double averageRiskScore) {
}
