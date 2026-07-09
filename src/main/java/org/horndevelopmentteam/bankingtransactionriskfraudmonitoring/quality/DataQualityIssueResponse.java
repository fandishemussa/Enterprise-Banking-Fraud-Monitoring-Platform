package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.quality;

import java.time.LocalDateTime;

public record DataQualityIssueResponse(
        String checkName,
        String recordIdentifier,
        String issueDescription,
        LocalDateTime detectedAt
) {

    public static DataQualityIssueResponse from(DataQualityIssueDetail issue) {
        return new DataQualityIssueResponse(
                issue.getDataQualityResult().getCheckName(),
                issue.getRecordIdentifier(),
                issue.getIssueDescription(),
                issue.getDetectedAt()
        );
    }
}
