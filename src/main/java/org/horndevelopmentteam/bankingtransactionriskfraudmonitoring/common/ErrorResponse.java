package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String message, String path, String traceId, LocalDateTime timestamp) {
}
