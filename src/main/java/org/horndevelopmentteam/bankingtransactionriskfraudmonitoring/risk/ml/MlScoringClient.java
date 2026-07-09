package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml;

import lombok.RequiredArgsConstructor;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Calls the FastAPI ml-service for a hybrid risk score. Never throws: any failure (service down,
 * timeout, malformed response) is logged, audited as ML_SCORE_FAILED, and surfaced as an empty
 * Optional so RiskScoringService can fall back to rule-only scoring without failing the
 * transaction-creation flow.
 */
@Service
@RequiredArgsConstructor
public class MlScoringClient {

    private static final Logger log = LoggerFactory.getLogger(MlScoringClient.class);

    private final RestClient mlServiceRestClient;
    private final AuditLogService auditLogService;

    public Optional<MlScoreResponse> score(MlScoreRequest request) {
        auditLogService.record(
                AuditEventType.ML_SCORE_REQUESTED,
                "BankingTransaction",
                request.transactionId(),
                null,
                null,
                "Requested ML score for transaction " + request.transactionId()
        );

        try {
            MlScoreResponse response = mlServiceRestClient.post()
                    .uri("/api/v1/score")
                    .body(request)
                    .retrieve()
                    .body(MlScoreResponse.class);

            if (response == null) {
                throw new IllegalStateException("ml-service returned an empty response body");
            }

            auditLogService.record(
                    AuditEventType.ML_SCORE_COMPLETED,
                    "BankingTransaction",
                    request.transactionId(),
                    null,
                    response.riskLevel() + " (" + response.mlScore() + ")",
                    "ML score received for transaction " + request.transactionId()
            );
            return Optional.of(response);
        } catch (Exception ex) {
            log.warn("ML scoring unavailable for transaction {}: {}", request.transactionId(), ex.getMessage());
            auditLogService.record(
                    AuditEventType.ML_SCORE_FAILED,
                    "BankingTransaction",
                    request.transactionId(),
                    null,
                    null,
                    "ML score request failed for transaction " + request.transactionId() + ": " + ex.getMessage()
            );
            return Optional.empty();
        }
    }
}
