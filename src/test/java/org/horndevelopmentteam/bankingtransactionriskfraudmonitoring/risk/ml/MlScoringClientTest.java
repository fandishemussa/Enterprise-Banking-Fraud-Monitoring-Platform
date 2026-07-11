package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.risk.ml;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditEventType;
import org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Exercises MlScoringClient.score()'s try/catch fallback path directly against an unreachable
 * ml-service (port 1, which refuses connections immediately) - this is the same code path that
 * protects transaction creation from ever failing because the ML service is down. Doesn't exercise
 * the @CircuitBreaker annotation itself (that needs a Spring AOP proxy, i.e. a full context), just
 * the underlying fallback-to-empty-Optional behavior it wraps.
 */
class MlScoringClientTest {

    @Test
    void returnsEmptyOptionalAndAuditsFailureWhenMlServiceUnreachable() {
        AuditLogService auditLogService = mock(AuditLogService.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(500);
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:1")
                .requestFactory(requestFactory)
                .build();

        MlScoringClient client = new MlScoringClient(restClient, auditLogService, meterRegistry);

        MlScoreRequest request = new MlScoreRequest(
                "TXN-1001", BigDecimal.valueOf(100), "PAYMENT", 12, 1, 0, 10,
                false, false, 0, 1.0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false);

        Optional<MlScoreResponse> result = client.score(request);

        assertThat(result).isEmpty();
        verify(auditLogService).record(eq(AuditEventType.ML_SCORE_FAILED), eq("BankingTransaction"),
                eq("TXN-1001"), eq(null), eq(null), org.mockito.ArgumentMatchers.anyString());
    }
}
