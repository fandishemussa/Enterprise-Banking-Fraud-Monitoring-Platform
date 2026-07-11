package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.caseinvestigation.timeline;

import jakarta.validation.constraints.NotBlank;

public record CaseNoteRequest(
        @NotBlank String noteText,
        Boolean internalOnly
) {
}
