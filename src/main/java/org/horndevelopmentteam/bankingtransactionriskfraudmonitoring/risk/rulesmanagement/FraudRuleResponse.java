package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;

import java.time.LocalDateTime;

public record FraudRuleResponse(
        String ruleId,
        String ruleCode,
        String name,
        String description,
        String category,
        boolean enabled,
        String thresholdValue,
        String secondaryThresholdValue,
        int scoreImpact,
        Integer secondaryScoreImpact,
        RiskLevel severity,
        FraudRuleType ruleType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public static FraudRuleResponse from(FraudRule rule) {
        return new FraudRuleResponse(
                rule.getRuleId(),
                rule.getRuleCode(),
                rule.getName(),
                rule.getDescription(),
                rule.getCategory(),
                Boolean.TRUE.equals(rule.getEnabled()),
                rule.getThresholdValue(),
                rule.getSecondaryThresholdValue(),
                rule.getScoreImpact(),
                rule.getSecondaryScoreImpact(),
                rule.getSeverity(),
                rule.getRuleType(),
                rule.getCreatedAt(),
                rule.getUpdatedAt(),
                rule.getUpdatedBy()
        );
    }
}
