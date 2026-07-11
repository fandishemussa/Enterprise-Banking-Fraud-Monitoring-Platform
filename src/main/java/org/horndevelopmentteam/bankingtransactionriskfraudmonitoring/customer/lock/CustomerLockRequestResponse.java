package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.customer.lock;

import java.time.LocalDateTime;

public record CustomerLockRequestResponse(
        String lockRequestId,
        String customerId,
        String requestedBy,
        String requestedByRole,
        String reason,
        CustomerLockRequestStatus status,
        String reviewedBy,
        LocalDateTime reviewedAt,
        String reviewNotes,
        LocalDateTime createdAt
) {

    public static CustomerLockRequestResponse from(CustomerLockRequest request) {
        return new CustomerLockRequestResponse(
                request.getLockRequestId(),
                request.getCustomer().getCustomerId(),
                request.getRequestedBy(),
                request.getRequestedByRole(),
                request.getReason(),
                request.getStatus(),
                request.getReviewedBy(),
                request.getReviewedAt(),
                request.getReviewNotes(),
                request.getCreatedAt()
        );
    }
}
