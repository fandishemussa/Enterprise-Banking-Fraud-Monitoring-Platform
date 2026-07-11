package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkUpdateAlertStatusRequest(
        @NotEmpty(message = "alertIds must not be empty") List<String> alertIds,
        @NotNull(message = "status is required") AlertStatus status
) {
}
