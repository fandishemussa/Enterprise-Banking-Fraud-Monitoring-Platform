package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.IdSequenceService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransaction;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion.CleanTransactionRepository;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineRun;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineStatus;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineTaskRun;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineTrackerService;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.pipeline.PipelineType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks.QualityCheck;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality.checks.QualityCheckOutcome;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataQualityService {

    private final List<QualityCheck> qualityChecks;
    private final CleanTransactionRepository cleanTransactionRepository;
    private final DataQualityRunRepository dataQualityRunRepository;
    private final DataQualityResultRepository dataQualityResultRepository;
    private final DataQualityIssueDetailRepository dataQualityIssueDetailRepository;
    private final PipelineTrackerService pipelineTrackerService;
    private final IdSequenceService idSequenceService;
    private final AuditLogService auditLogService;

    @Transactional
    public DataQualityRunResponse runChecks(String triggeredBy) {
        PipelineRun pipelineRun = pipelineTrackerService.startRun(PipelineType.DATA_QUALITY, triggeredBy);

        DataQualityRun run = dataQualityRunRepository.save(DataQualityRun.builder()
                .runId(idSequenceService.next("DQ"))
                .pipelineRun(pipelineRun)
                .status(PipelineStatus.RUNNING)
                .triggeredBy(triggeredBy)
                .totalRecordsChecked(0)
                .totalIssuesFound(0)
                .startedAt(LocalDateTime.now())
                .build());

        List<CleanTransaction> records = cleanTransactionRepository.findAll();
        int totalIssues = 0;

        for (QualityCheck check : qualityChecks) {
            PipelineTaskRun task = pipelineTrackerService.startTask(pipelineRun, check.getClass().getSimpleName());
            QualityCheckOutcome outcome = check.run(records);

            DataQualityResult result = dataQualityResultRepository.save(DataQualityResult.builder()
                    .dataQualityRun(run)
                    .checkName(outcome.checkName())
                    .recordsChecked(outcome.recordsChecked())
                    .recordsPassed(outcome.recordsPassed())
                    .recordsFailed(outcome.recordsFailed())
                    .passed(outcome.passed())
                    .build());

            for (QualityCheckOutcome.Issue issue : outcome.issues()) {
                dataQualityIssueDetailRepository.save(DataQualityIssueDetail.builder()
                        .dataQualityResult(result)
                        .recordIdentifier(issue.recordIdentifier())
                        .issueDescription(issue.description())
                        .detectedAt(LocalDateTime.now())
                        .build());
            }

            totalIssues += outcome.recordsFailed();
            pipelineTrackerService.completeTask(task, outcome.recordsChecked());
        }

        run.setTotalRecordsChecked(records.size());
        run.setTotalIssuesFound(totalIssues);
        run.setFinishedAt(LocalDateTime.now());
        run.setStatus(totalIssues == 0 ? PipelineStatus.SUCCESS : PipelineStatus.PARTIAL_SUCCESS);
        DataQualityRun saved = dataQualityRunRepository.save(run);

        pipelineTrackerService.completeRun(pipelineRun, records.size(), records.size() - totalIssues, totalIssues, 0);

        auditLogService.record(
                AuditEventType.DATA_QUALITY_CHECK_COMPLETED,
                "DataQualityRun",
                saved.getRunId(),
                null,
                saved.getStatus().name(),
                "Data quality run " + saved.getRunId() + " completed: " + totalIssues
                        + " issue(s) found across " + records.size() + " records"
        );

        return DataQualityRunResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DataQualityRunResponse> getAllRuns() {
        return dataQualityRunRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(DataQualityRunResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DataQualityResultResponse> getAllResults() {
        return dataQualityResultRepository.findTop100ByOrderByIdDesc().stream()
                .map(DataQualityResultResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DataQualityIssueResponse> getAllIssues() {
        return dataQualityIssueDetailRepository.findTop200ByOrderByDetectedAtDesc().stream()
                .map(DataQualityIssueResponse::from)
                .toList();
    }
}
