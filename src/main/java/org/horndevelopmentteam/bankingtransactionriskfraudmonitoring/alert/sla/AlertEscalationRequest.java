package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert.sla;

import jakarta.validation.constraints.NotBlank;

public record AlertEscalationRequest(
        @NotBlank String escalatedTo,
        String reason
) {
}
