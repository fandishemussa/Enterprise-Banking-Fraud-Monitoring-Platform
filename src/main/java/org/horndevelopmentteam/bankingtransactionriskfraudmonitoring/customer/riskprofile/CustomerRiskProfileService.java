package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.riskprofile;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlert;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.FraudAlertResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.CaseDecision;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.CaseResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.CaseStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.InvestigationCase;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.InvestigationCaseRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.Customer;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.CustomerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScoreRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement.FraudRuleConfigService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Builds the "Customer Risk 360" profile by aggregating this customer's transactions, risk
 * scores, alerts, and cases in memory. Deliberately not pushed down into custom repository
 * aggregate queries (COUNT/SUM/AVG/GROUP BY) - at this system's per-customer data volumes, loading
 * the lists once and deriving every metric from them in Java is simpler to read and verify than a
 * dozen narrow derived-query methods, and avoids running N separate queries per page load.
 */
@Service
@RequiredArgsConstructor
public class CustomerRiskProfileService {

    private static final int TREND_WINDOW_DAYS = 30;
    private static final int RECENT_LIMIT = 5;
    private static final Set<String> DEFAULT_HIGH_RISK_CATEGORIES = Set.of("CRYPTO", "GAMBLING", "HIGH_RISK_TRANSFER");

    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final InvestigationCaseRepository investigationCaseRepository;
    private final FraudRuleConfigService fraudRuleConfigService;

    @Transactional(readOnly = true)
    public CustomerRiskProfileResponse getRiskProfile(String customerId) {
        Customer customer = customerService.findByPublicIdOrThrow(customerId);

        List<BankingTransaction> transactions = transactionRepository.findByCustomer(customer);
        List<RiskScore> riskScores = riskScoreRepository.findByTransactionIn(transactions);
        List<FraudAlert> alerts = fraudAlertRepository.findByCustomer(customer);
        List<InvestigationCase> cases = investigationCaseRepository.findByCustomer(customer);

        return new CustomerRiskProfileResponse(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCountry(),
                customer.getRiskLevel(),
                customer.getStatus(),
                customer.getCreatedAt(),

                transactions.size(),
                totalAmount(transactions),
                averageAmount(transactions),
                maxAmount(transactions),
                alerts.size(),
                alerts.stream().filter(a -> a.getStatus() == AlertStatus.OPEN).count(),
                alerts.stream().filter(a -> a.getStatus() == AlertStatus.CONFIRMED_FRAUD).count(),
                alerts.stream().filter(a -> a.getStatus() == AlertStatus.FALSE_POSITIVE).count(),
                cases.size(),
                cases.stream().filter(c -> c.getStatus() != CaseStatus.RESOLVED && c.getStatus() != CaseStatus.CLOSED).count(),
                riskScores.stream().filter(r -> r.getRiskLevel() == RiskLevel.CRITICAL).count(),
                riskScores.stream().filter(r -> r.getRiskLevel() == RiskLevel.HIGH).count(),

                buildBehaviorSummary(transactions, alerts),

                recentTransactions(transactions),
                recentRiskScores(riskScores),
                recentAlerts(alerts),
                recentCases(cases),

                riskTrend(riskScores),
                transactionVolumeTrend(transactions),
                alertTrend(alerts)
        );
    }

    private BigDecimal totalAmount(List<BankingTransaction> transactions) {
        return transactions.stream().map(BankingTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal averageAmount(List<BankingTransaction> transactions) {
        if (transactions.isEmpty()) return BigDecimal.ZERO;
        return totalAmount(transactions).divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal maxAmount(List<BankingTransaction> transactions) {
        return transactions.stream().map(BankingTransaction::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private CustomerBehaviorSummary buildBehaviorSummary(List<BankingTransaction> transactions, List<FraudAlert> alerts) {
        Set<String> highRiskCategories = fraudRuleConfigService.findEnabledConfig("HIGH_RISK_MERCHANT_RULE")
                .map(rule -> rule.getThresholdValue() != null && !rule.getThresholdValue().isBlank()
                        ? Arrays.stream(rule.getThresholdValue().split(",")).map(String::trim).map(String::toUpperCase).collect(Collectors.toSet())
                        : DEFAULT_HIGH_RISK_CATEGORIES)
                .orElse(DEFAULT_HIGH_RISK_CATEGORIES);

        List<String> devicesUsed = transactions.stream()
                .map(BankingTransaction::getDeviceId).filter(d -> d != null && !d.isBlank()).distinct().toList();
        List<String> countriesUsed = transactions.stream()
                .map(BankingTransaction::getCountry).filter(c -> c != null && !c.isBlank()).distinct().toList();
        List<String> highRiskCategoriesUsed = transactions.stream()
                .map(BankingTransaction::getMerchantCategory)
                .filter(c -> c != null && highRiskCategories.contains(c.toUpperCase()))
                .distinct().toList();

        String mostUsedChannel = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getChannel().name(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        String mostUsedCountry = transactions.stream()
                .collect(Collectors.groupingBy(BankingTransaction::getCountry, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        LocalDateTime lastTransactionAt = transactions.stream()
                .map(BankingTransaction::getCreatedAt).max(Comparator.naturalOrder()).orElse(null);
        LocalDateTime lastAlertAt = alerts.stream()
                .map(FraudAlert::getCreatedAt).max(Comparator.naturalOrder()).orElse(null);

        return new CustomerBehaviorSummary(devicesUsed, countriesUsed, highRiskCategoriesUsed,
                mostUsedChannel, mostUsedCountry, lastTransactionAt, lastAlertAt);
    }

    private List<TransactionResponse> recentTransactions(List<BankingTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(BankingTransaction::getCreatedAt).reversed())
                .limit(RECENT_LIMIT)
                .map(TransactionResponse::from)
                .toList();
    }

    private List<RiskScoreResponse> recentRiskScores(List<RiskScore> riskScores) {
        return riskScores.stream()
                .sorted(Comparator.comparing(RiskScore::getCreatedAt).reversed())
                .limit(RECENT_LIMIT)
                .map(RiskScoreResponse::from)
                .toList();
    }

    private List<FraudAlertResponse> recentAlerts(List<FraudAlert> alerts) {
        return alerts.stream()
                .sorted(Comparator.comparing(FraudAlert::getCreatedAt).reversed())
                .limit(RECENT_LIMIT)
                .map(FraudAlertResponse::from)
                .toList();
    }

    private List<CaseResponse> recentCases(List<InvestigationCase> cases) {
        return cases.stream()
                .sorted(Comparator.comparing(InvestigationCase::getCreatedAt).reversed())
                .limit(RECENT_LIMIT)
                .map(CaseResponse::from)
                .toList();
    }

    private List<CustomerRiskTrendPoint> riskTrend(List<RiskScore> riskScores) {
        LocalDate windowStart = LocalDate.now(ZoneOffset.UTC).minusDays(TREND_WINDOW_DAYS);
        Map<LocalDate, List<Integer>> byDate = new TreeMap<>();
        for (RiskScore score : riskScores) {
            LocalDate date = score.getCreatedAt().toLocalDate();
            if (date.isBefore(windowStart)) continue;
            byDate.computeIfAbsent(date, d -> new java.util.ArrayList<>()).add(score.getFinalScore());
        }
        return byDate.entrySet().stream()
                .map(e -> new CustomerRiskTrendPoint(e.getKey(),
                        e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)))
                .toList();
    }

    private List<CustomerTransactionTrendPoint> transactionVolumeTrend(List<BankingTransaction> transactions) {
        LocalDate windowStart = LocalDate.now(ZoneOffset.UTC).minusDays(TREND_WINDOW_DAYS);
        Map<LocalDate, List<BankingTransaction>> byDate = new TreeMap<>();
        for (BankingTransaction transaction : transactions) {
            LocalDate date = transaction.getCreatedAt().toLocalDate();
            if (date.isBefore(windowStart)) continue;
            byDate.computeIfAbsent(date, d -> new java.util.ArrayList<>()).add(transaction);
        }
        return byDate.entrySet().stream()
                .map(e -> new CustomerTransactionTrendPoint(e.getKey(), e.getValue().size(), totalAmount(e.getValue())))
                .toList();
    }

    private List<CustomerTransactionTrendPoint> alertTrend(List<FraudAlert> alerts) {
        LocalDate windowStart = LocalDate.now(ZoneOffset.UTC).minusDays(TREND_WINDOW_DAYS);
        Map<LocalDate, Long> byDate = new TreeMap<>();
        for (FraudAlert alert : alerts) {
            LocalDate date = alert.getCreatedAt().toLocalDate();
            if (date.isBefore(windowStart)) continue;
            byDate.merge(date, 1L, Long::sum);
        }
        return byDate.entrySet().stream()
                .map(e -> new CustomerTransactionTrendPoint(e.getKey(), e.getValue(), BigDecimal.ZERO))
                .toList();
    }
}
