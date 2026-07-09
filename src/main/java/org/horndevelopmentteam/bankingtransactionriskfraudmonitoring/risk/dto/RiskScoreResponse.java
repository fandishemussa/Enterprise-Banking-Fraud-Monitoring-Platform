package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.dto;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.ScoringSource;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.RiskScore;

import java.time.LocalDateTime;

public record RiskScoreResponse(
        String transactionId,
        Integer ruleScore,
        Integer mlScore,
        Integer finalScore,
        RiskLevel riskLevel,
        String triggeredRules,
        String explanation,
        String mlExplanation,
        ScoringSource scoringSource,
        LocalDateTime createdAt
) {

    public static RiskScoreResponse from(RiskScore riskScore) {
        return new RiskScoreResponse(
                riskScore.getTransaction().getTransactionId(),
                riskScore.getRuleScore(),
                riskScore.getMlScore(),
                riskScore.getFinalScore(),
                riskScore.getRiskLevel(),
                riskScore.getTriggeredRules(),
                riskScore.getExplanation(),
                riskScore.getMlExplanation(),
                riskScore.getScoringSource(),
                riskScore.getCreatedAt()
        );
    }
}
