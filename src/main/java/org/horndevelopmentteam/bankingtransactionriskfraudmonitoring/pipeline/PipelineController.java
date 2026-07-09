package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineQueryService pipelineQueryService;

    @GetMapping("/runs")
    public ApiResponse<List<PipelineRunResponse>> getAllRuns() {
        return ApiResponse.success(pipelineQueryService.getAllRuns());
    }

    @GetMapping("/runs/{runId}")
    public ApiResponse<PipelineRunResponse> getRun(@PathVariable String runId) {
        return ApiResponse.success(pipelineQueryService.getRunByPublicId(runId));
    }

    @GetMapping("/metrics")
    public ApiResponse<PipelineMetricsResponse> getMetrics() {
        return ApiResponse.success(pipelineQueryService.getMetrics());
    }

    @GetMapping("/errors")
    public ApiResponse<List<PipelineErrorResponse>> getErrors() {
        return ApiResponse.success(pipelineQueryService.getRecentErrors());
    }
}
