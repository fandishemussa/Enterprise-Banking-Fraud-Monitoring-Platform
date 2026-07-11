package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto.RiskScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.ScoringSource;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml.MlScoreRequest;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml.MlScoreResponse;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml.MlScoringClient;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules.RiskRule;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rules.RuleResult;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.BankingTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.transaction.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskScoringService {

    private static final double RULE_WEIGHT = 0.7;
    private static final double ML_WEIGHT = 0.3;

    private final List<RiskRule> riskRules;
    private final RiskScoreRepository riskScoreRepository;
    private final TransactionRepository transactionRepository;
    private final MlScoringClient mlScoringClient;
    private final AuditLogService auditLogService;

    @Transactional
    public RiskScore score(BankingTransaction transaction) {
        List<RuleResult> results = riskRules.stream()
                .map(rule -> rule.evaluate(transaction))
                .toList();

        int ruleScore = results.stream().mapToInt(RuleResult::points).sum();
        int cappedRuleScore = Math.min(ruleScore, 100);

        // Includes ruleCode/severity alongside the name+score so the stored explanation is
        // self-sufficient for the fraud-rules-management UI without a second lookup.
        String triggeredRules = results.stream()
                .filter(RuleResult::triggered)
                .map(r -> r.ruleName() + (r.ruleCode() != null ? "[" + r.ruleCode() + "]" : "")
                        + "(+" + r.points() + (r.severity() != null ? ", " + r.severity() : "") + ")")
                .collect(Collectors.joining(", "));

        String explanation = results.stream()
                .filter(RuleResult::triggered)
                .map(RuleResult::reason)
                .collect(Collectors.joining("; "));
        if (explanation.isBlank()) {
            explanation = "No risk rules triggered";
        }

        Optional<MlScoreResponse> mlResponse = mlScoringClient.score(buildMlRequest(transaction, results));

        int finalScore;
        Integer mlScore;
        String mlExplanation;
        ScoringSource scoringSource;

        if (mlResponse.isPresent()) {
            MlScoreResponse ml = mlResponse.get();
            mlScore = ml.mlScore();
            finalScore = BigDecimal.valueOf(cappedRuleScore).multiply(BigDecimal.valueOf(RULE_WEIGHT))
                    .add(BigDecimal.valueOf(mlScore).multiply(BigDecimal.valueOf(ML_WEIGHT)))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
            finalScore = Math.min(finalScore, 100);
            mlExplanation = String.join("; ", ml.explanation());
            scoringSource = ScoringSource.HYBRID;
        } else {
            mlScore = null;
            finalScore = cappedRuleScore;
            mlExplanation = null;
            scoringSource = ScoringSource.RULE_ONLY;
        }

        RiskLevel riskLevel = RiskLevel.fromScore(finalScore);

        RiskScore riskScore = RiskScore.builder()
                .transaction(transaction)
                .ruleScore(ruleScore)
                .mlScore(mlScore)
                .finalScore(finalScore)
                .riskLevel(riskLevel)
                .triggeredRules(triggeredRules)
                .explanation(explanation)
                .mlExplanation(mlExplanation)
                .scoringSource(scoringSource)
                .createdAt(LocalDateTime.now())
                .build();
        RiskScore saved = riskScoreRepository.save(riskScore);

        auditLogService.record(
                AuditEventType.RISK_SCORE_GENERATED,
                "RiskScore",
                transaction.getTransactionId(),
                null,
                riskLevel.name() + " (" + finalScore + ", " + scoringSource + ")",
                "Risk score generated for transaction " + transaction.getTransactionId()
        );

        return saved;
    }

    private MlScoreRequest buildMlRequest(BankingTransaction transaction, List<RuleResult> results) {
        boolean newDevice = ruleTriggered(results, "NewDeviceRule");
        boolean newCountry = ruleTriggered(results, "NewCountryRule");
        boolean highRiskMerchant = ruleTriggered(results, "HighRiskMerchantRule");

        long recentTransactionCount = transactionRepository.countByCustomerAndCreatedAtAfter(
                transaction.getCustomer(), LocalDateTime.now().minusMinutes(10));

        List<BankingTransaction> pastTransactions = transactionRepository.findByCustomer(transaction.getCustomer()).stream()
                .filter(t -> !t.getId().equals(transaction.getId()))
                .toList();
        double averagePastAmount = pastTransactions.stream()
                .mapToDouble(t -> t.getAmount().doubleValue())
                .average()
                .orElse(0.0);
        double customerAverageAmountRatio = averagePastAmount > 0
                ? transaction.getAmount().doubleValue() / averagePastAmount
                : 1.0;

        return new MlScoreRequest(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getTransactionType().name(),
                transaction.getCreatedAt().getHour(),
                (int) Math.min(recentTransactionCount, Integer.MAX_VALUE),
                0,
                newCountry ? 80 : 10,
                newDevice,
                newCountry,
                highRiskMerchant ? 80 : 10,
                customerAverageAmountRatio,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false
        );
    }

    private boolean ruleTriggered(List<RuleResult> results, String ruleName) {
        return results.stream().anyMatch(r -> r.ruleName().equals(ruleName) && r.triggered());
    }

    @Transactional(readOnly = true)
    public Page<RiskScoreResponse> getAllRiskScores(Pageable pageable) {
        return riskScoreRepository.findAll(pageable).map(RiskScoreResponse::from);
    }

    @Transactional(readOnly = true)
    public RiskScoreResponse getRiskScoreForTransaction(String transactionId) {
        return riskScoreRepository.findByTransaction_TransactionId(transactionId)
                .map(RiskScoreResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Risk score not found for transaction: " + transactionId));
    }
}
