package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.AlertPriority;

public record AlertSlaPolicyResponse(
        String policyId,
        AlertPriority priority,
        int responseTimeMinutes,
        int resolutionTimeMinutes,
        boolean enabled
) {
    public static AlertSlaPolicyResponse from(AlertSlaPolicy policy) {
        return new AlertSlaPolicyResponse(
                policy.getPolicyId(),
                policy.getPriority(),
                policy.getResponseTimeMinutes(),
                policy.getResolutionTimeMinutes(),
                Boolean.TRUE.equals(policy.getEnabled())
        );
    }
}
