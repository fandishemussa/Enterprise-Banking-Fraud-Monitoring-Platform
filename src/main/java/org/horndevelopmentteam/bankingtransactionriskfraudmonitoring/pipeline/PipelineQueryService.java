package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PipelineQueryService {

    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineTaskRunRepository pipelineTaskRunRepository;
    private final PipelineErrorRepository pipelineErrorRepository;

    @Transactional(readOnly = true)
    public List<PipelineRunResponse> getAllRuns() {
        return pipelineRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(run -> PipelineRunResponse.from(run, tasksFor(run)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PipelineRunResponse getRunByPublicId(String runId) {
        PipelineRun run = pipelineRunRepository.findByRunId(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline run not found: " + runId));
        return PipelineRunResponse.from(run, tasksFor(run));
    }

    @Transactional(readOnly = true)
    public PipelineMetricsResponse getMetrics() {
        List<PipelineRun> runs = pipelineRunRepository.findAll();
        long totalRuns = runs.size();
        long successfulRuns = runs.stream().filter(r -> r.getStatus() == PipelineStatus.SUCCESS).count();
        long failedRuns = runs.stream().filter(r -> r.getStatus() == PipelineStatus.FAILED).count();
        double successRate = totalRuns == 0 ? 0.0 : (successfulRuns * 100.0) / totalRuns;

        java.util.OptionalDouble averageDuration = runs.stream()
                .map(PipelineRun::getDurationMs)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .average();
        Double averageDurationMs = averageDuration.isPresent() ? averageDuration.getAsDouble() : null;

        var lastSuccessful = pipelineRunRepository.findFirstByStatusOrderByFinishedAtDesc(PipelineStatus.SUCCESS);
        var lastFailed = pipelineRunRepository.findFirstByStatusOrderByFinishedAtDesc(PipelineStatus.FAILED);

        return new PipelineMetricsResponse(
                totalRuns,
                successfulRuns,
                failedRuns,
                successRate,
                averageDurationMs,
                lastSuccessful.map(PipelineRun::getFinishedAt).orElse(null),
                lastFailed.map(PipelineRun::getFailureReason).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public List<PipelineErrorResponse> getRecentErrors() {
        return pipelineErrorRepository.findTop50ByOrderByOccurredAtDesc().stream()
                .map(PipelineErrorResponse::from)
                .toList();
    }

    private List<PipelineTaskRunResponse> tasksFor(PipelineRun run) {
        return pipelineTaskRunRepository.findByPipelineRunOrderByStartedAtAsc(run).stream()
                .map(PipelineTaskRunResponse::from)
                .toList();
    }
}
