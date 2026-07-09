package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

public record UpdateCaseRequest(
        CaseStatus status,
        String assignedTo,
        String notes
) {
}
