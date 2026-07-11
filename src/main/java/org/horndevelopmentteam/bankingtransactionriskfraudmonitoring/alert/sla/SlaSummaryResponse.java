package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

public record SlaSummaryResponse(
        long totalAlerts,
        long onTrack,
        long nearBreach,
        long breached,
        long completed,
        double slaComplianceRatePercent,
        double averageResponseTimeMinutes,
        double averageResolutionTimeMinutes
) {
}
