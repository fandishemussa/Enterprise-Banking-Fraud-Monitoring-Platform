package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import java.time.LocalDateTime;

public record AlertSlaResultResponse(
        String alertId,
        String policyId,
        LocalDateTime responseDeadline,
        LocalDateTime resolutionDeadline,
        LocalDateTime firstAcknowledgedAt,
        LocalDateTime resolvedAt,
        boolean responseBreached,
        boolean resolutionBreached,
        SlaStatus status
) {
    public static AlertSlaResultResponse from(AlertSlaResult result) {
        return new AlertSlaResultResponse(
                result.getAlertId(),
                result.getPolicyId(),
                result.getResponseDeadline(),
                result.getResolutionDeadline(),
                result.getFirstAcknowledgedAt(),
                result.getResolvedAt(),
                Boolean.TRUE.equals(result.getResponseBreached()),
                Boolean.TRUE.equals(result.getResolutionBreached()),
                result.getStatus()
        );
    }
}
