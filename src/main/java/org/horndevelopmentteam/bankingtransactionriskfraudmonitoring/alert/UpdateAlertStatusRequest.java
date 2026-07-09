package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import jakarta.validation.constraints.NotNull;

public record UpdateAlertStatusRequest(@NotNull(message = "status is required") AlertStatus status) {
}
