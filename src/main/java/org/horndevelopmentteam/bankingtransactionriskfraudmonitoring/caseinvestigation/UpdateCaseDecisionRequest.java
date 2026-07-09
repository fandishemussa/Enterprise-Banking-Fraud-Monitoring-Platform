package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import jakarta.validation.constraints.NotNull;

public record UpdateCaseDecisionRequest(@NotNull(message = "decision is required") CaseDecision decision) {
}
