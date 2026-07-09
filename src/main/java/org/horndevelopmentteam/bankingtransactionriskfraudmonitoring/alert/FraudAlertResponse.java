package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.alert;

import java.time.LocalDateTime;

public record FraudAlertResponse(
        String alertId,
        String transactionId,
        String customerId,
        Integer riskScoreValue,
        AlertType alertType,
        AlertPriority priority,
        String message,
        AlertStatus status,
        String assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {

    public static FraudAlertResponse from(FraudAlert alert) {
        return new FraudAlertResponse(
                alert.getAlertId(),
                alert.getTransaction().getTransactionId(),
                alert.getCustomer().getCustomerId(),
                alert.getRiskScore().getFinalScore(),
                alert.getAlertType(),
                alert.getPriority(),
                alert.getMessage(),
                alert.getStatus(),
                alert.getAssignedTo(),
                alert.getCreatedAt(),
                alert.getUpdatedAt(),
                alert.getResolvedAt()
        );
    }
}
