package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkUpdateCasesRequest(
        @NotEmpty(message = "caseIds must not be empty") List<String> caseIds,
        CaseStatus status,
        String assignedTo
) {
}
