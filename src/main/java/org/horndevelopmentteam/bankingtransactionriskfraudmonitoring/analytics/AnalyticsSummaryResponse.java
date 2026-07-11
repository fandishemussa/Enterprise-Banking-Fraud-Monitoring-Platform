package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.analytics;

import java.util.Map;

public record AnalyticsSummaryResponse(
        int totalAlerts,
        int totalCases,
        Map<String, Long> alertsByMerchantCategory,
        Map<String, Long> alertsByCountry,
        Map<Integer, Long> alertsByHourOfDay,
        Map<String, Long> casesByStatus,
        Map<String, Long> casesByDecision
) {
}
