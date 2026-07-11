package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.analytics;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlert;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.InvestigationCase;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.InvestigationCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Platform-wide reporting aggregates for the Analytics page - deliberately unscoped by the
 * assignment-visibility rule that governs the Alerts/Cases list views (see
 * FraudAlertService#getAlertsVisibleTo): this is a management/compliance reporting surface, gated
 * to ADMIN/VIEWER at the controller level instead, not a personal work queue.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final FraudAlertRepository fraudAlertRepository;
    private final InvestigationCaseRepository investigationCaseRepository;

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {
        List<FraudAlert> alerts = fraudAlertRepository.findAll();
        List<InvestigationCase> cases = investigationCaseRepository.findAll();

        Map<String, Long> byMerchantCategory = groupAndCount(alerts,
                alert -> alert.getTransaction().getMerchantCategory() != null
                        ? alert.getTransaction().getMerchantCategory()
                        : "UNKNOWN");

        Map<String, Long> byCountry = groupAndCount(alerts, alert -> alert.getTransaction().getCountry());

        Map<Integer, Long> byHour = alerts.stream()
                .collect(Collectors.groupingBy(
                        alert -> alert.getTransaction().getCreatedAt().getHour(),
                        () -> new LinkedHashMap<>(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> byCaseStatus = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        Map<String, Long> byCaseDecision = cases.stream()
                .collect(Collectors.groupingBy(c -> c.getDecision().name(), Collectors.counting()));

        return new AnalyticsSummaryResponse(
                alerts.size(), cases.size(), byMerchantCategory, byCountry, byHour, byCaseStatus, byCaseDecision);
    }

    private Map<String, Long> groupAndCount(List<FraudAlert> alerts, Function<FraudAlert, String> classifier) {
        return alerts.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}
