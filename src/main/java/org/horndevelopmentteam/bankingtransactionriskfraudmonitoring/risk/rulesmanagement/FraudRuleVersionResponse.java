package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.rulesmanagement;

import java.time.LocalDateTime;

public record FraudRuleVersionResponse(
        String ruleId,
        int versionNumber,
        String oldConfigJson,
        String newConfigJson,
        String changedBy,
        String changeReason,
        LocalDateTime createdAt
) {
    public static FraudRuleVersionResponse from(FraudRuleVersion version) {
        return new FraudRuleVersionResponse(
                version.getRuleId(),
                version.getVersionNumber(),
                version.getOldConfigJson(),
                version.getNewConfigJson(),
                version.getChangedBy(),
                version.getChangeReason(),
                version.getCreatedAt()
        );
    }
}
