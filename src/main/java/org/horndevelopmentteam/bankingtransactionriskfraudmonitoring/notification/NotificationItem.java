package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.notification;

import java.time.LocalDateTime;

public record NotificationItem(
        String id,
        NotificationType type,
        String title,
        String message,
        String referenceId,
        LocalDateTime createdAt
) {
}
