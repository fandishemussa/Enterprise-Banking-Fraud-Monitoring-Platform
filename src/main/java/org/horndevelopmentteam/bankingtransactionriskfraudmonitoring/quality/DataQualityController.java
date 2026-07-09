package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-quality")
@RequiredArgsConstructor
public class DataQualityController {

    private static final String DEFAULT_TRIGGER = "manual";

    private final DataQualityService dataQualityService;

    @PostMapping("/run")
    public ApiResponse<DataQualityRunResponse> runChecks() {
        return ApiResponse.success("Data quality run completed", dataQualityService.runChecks(DEFAULT_TRIGGER));
    }

    @GetMapping("/runs")
    public ApiResponse<List<DataQualityRunResponse>> getAllRuns() {
        return ApiResponse.success(dataQualityService.getAllRuns());
    }

    @GetMapping("/results")
    public ApiResponse<List<DataQualityResultResponse>> getAllResults() {
        return ApiResponse.success(dataQualityService.getAllResults());
    }

    @GetMapping("/issues")
    public ApiResponse<List<DataQualityIssueResponse>> getAllIssues() {
        return ApiResponse.success(dataQualityService.getAllIssues());
    }
}
