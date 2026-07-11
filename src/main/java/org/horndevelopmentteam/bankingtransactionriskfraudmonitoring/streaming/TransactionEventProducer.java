package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventProducer.class);
    private static final int SEND_TIMEOUT_SECONDS = 3;

    private final KafkaTemplate<String, String> kafkaTemplate;

    // Not Spring-managed for the same reason as FraudRuleService's ObjectMapper - only used for
    // an internal wire-format string, not HTTP (de)serialization.
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /** Publishes any JSON-serializable payload to the given topic, blocking briefly to surface a
     * broker-unreachable failure synchronously rather than losing it in an unobserved future. */
    public void publish(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Failed to publish to topic '{}': {}", topic, ex.getMessage());
            throw new StreamingUnavailableException("Failed to publish to topic " + topic, ex);
        }
    }
}
