package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.ingestion;

import java.time.LocalDateTime;

public record RejectedTransactionResponse(
        String ingestionRunId,
        String transactionId,
        RejectionReason reason,
        String reasonDetail,
        LocalDateTime rejectedAt
) {

    public static RejectedTransactionResponse from(RejectedTransaction rejected) {
        return new RejectedTransactionResponse(
                rejected.getIngestionRun().getRunId(),
                rejected.getStagingTransaction().getTransactionId(),
                rejected.getReason(),
                rejected.getReasonDetail(),
                rejected.getRejectedAt()
        );
    }
}
