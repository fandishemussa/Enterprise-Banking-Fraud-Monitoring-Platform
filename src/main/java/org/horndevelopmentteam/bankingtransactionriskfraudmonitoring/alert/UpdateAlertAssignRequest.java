package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.constraints.NotBlank;

public record UpdateAlertAssignRequest(@NotBlank(message = "assignedTo is required") String assignedTo) {
}
