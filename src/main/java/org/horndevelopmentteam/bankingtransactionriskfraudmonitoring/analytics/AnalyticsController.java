package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.analytics;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/v1/analytics/summary")
    @PreAuthorize("@access.allow('ADMIN', 'VIEWER')")
    public ApiResponse<AnalyticsSummaryResponse> getSummary() {
        return ApiResponse.success(analyticsService.getSummary());
    }
}
