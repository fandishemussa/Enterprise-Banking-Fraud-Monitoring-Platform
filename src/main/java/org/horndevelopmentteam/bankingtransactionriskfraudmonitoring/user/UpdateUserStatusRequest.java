package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull(message = "status is required") UserStatus status) {
}
