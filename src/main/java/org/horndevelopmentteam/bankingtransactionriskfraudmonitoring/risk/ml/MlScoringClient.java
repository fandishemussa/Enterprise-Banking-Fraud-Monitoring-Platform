package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
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
 *
 * The circuit breaker (see resilience4j.circuitbreaker.instances.mlService in application.yml)
 * exists on top of that per-call timeout/fallback: if the ML service is degraded but not fully
 * down (e.g. slow, half-responding), every request paying the full connect/read timeout serially
 * would back up the transaction-creation path. Once the failure rate crosses the configured
 * threshold, the breaker opens and short-circuits new calls straight to the fallback for a cooldown
 * window, instead of each one waiting out the timeout individually.
 */
@Service
@RequiredArgsConstructor
public class MlScoringClient {

    private static final Logger log = LoggerFactory.getLogger(MlScoringClient.class);

    private final RestClient mlServiceRestClient;
    private final AuditLogService auditLogService;
    private final MeterRegistry meterRegistry;

    @CircuitBreaker(name = "mlService", fallbackMethod = "scoreFallback")
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
            meterRegistry.counter("ml_score_requests_total", "outcome", "success").increment();
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
            meterRegistry.counter("ml_score_requests_total", "outcome", "fallback").increment();
            return Optional.empty();
        }
    }

    /** Invoked when the circuit is open (CallNotPermittedException) instead of letting every
     * request pay the connect/read timeout while the ML service is known to be degraded. */
    private Optional<MlScoreResponse> scoreFallback(MlScoreRequest request, Throwable throwable) {
        log.warn("ML scoring circuit breaker short-circuited call for transaction {}: {}",
                request.transactionId(), throwable.getMessage());
        auditLogService.record(
                AuditEventType.ML_SCORE_FAILED,
                "BankingTransaction",
                request.transactionId(),
                null,
                null,
                "ML score request short-circuited by circuit breaker for transaction "
                        + request.transactionId() + ": " + throwable.getMessage()
        );
        meterRegistry.counter("ml_score_requests_total", "outcome", "circuit_open").increment();
        return Optional.empty();
    }
}
