package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkAssignAlertsRequest(
        @NotEmpty(message = "alertIds must not be empty") List<String> alertIds,
        @NotBlank(message = "assignedTo is required") String assignedTo
) {
}
