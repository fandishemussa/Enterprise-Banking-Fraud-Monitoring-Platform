package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

public record DataQualityResultResponse(
        String runId,
        String checkName,
        int recordsChecked,
        int recordsPassed,
        int recordsFailed,
        boolean passed
) {

    public static DataQualityResultResponse from(DataQualityResult result) {
        return new DataQualityResultResponse(
                result.getDataQualityRun().getRunId(),
                result.getCheckName(),
                result.getRecordsChecked(),
                result.getRecordsPassed(),
                result.getRecordsFailed(),
                result.isPassed()
        );
    }
}
