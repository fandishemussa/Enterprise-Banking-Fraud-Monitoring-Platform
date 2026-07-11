package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertSlaPolicyRequest(
        @NotNull @Positive Integer responseTimeMinutes,
        @NotNull @Positive Integer resolutionTimeMinutes,
        Boolean enabled
) {
}
