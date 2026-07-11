package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Also reused for alertTrend (count = alert count that day, totalAmount = ZERO in that case). */
public record CustomerTransactionTrendPoint(LocalDate date, long count, BigDecimal totalAmount) {
}
