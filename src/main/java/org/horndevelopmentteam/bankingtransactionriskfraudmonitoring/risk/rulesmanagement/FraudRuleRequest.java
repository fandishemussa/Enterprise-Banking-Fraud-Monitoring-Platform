package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.enums.RiskLevel;

public record FraudRuleRequest(
        @NotBlank String ruleCode,
        @NotBlank String name,
        String description,
        String category,
        Boolean enabled,
        String thresholdValue,
        String secondaryThresholdValue,
        @NotNull Integer scoreImpact,
        Integer secondaryScoreImpact,
        @NotNull RiskLevel severity,
        @NotNull FraudRuleType ruleType,
        String changeReason
) {
}
