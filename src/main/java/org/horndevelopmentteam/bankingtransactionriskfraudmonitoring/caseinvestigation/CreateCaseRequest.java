package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation;

import jakarta.validation.constraints.NotBlank;

public record CreateCaseRequest(
        @NotBlank(message = "alertId is required") String alertId,
        String assignedTo,
        String notes
) {
}
