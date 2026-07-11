package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final StreamingTransactionService streamingTransactionService;
    private final DeadLetterEventPublisher deadLetterEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @KafkaListener(topics = "${kafka.topics.raw-transactions}", groupId = "${spring.kafka.consumer.group-id}")
    public void onRawTransaction(String payload) {
        TransactionEvent event;
        try {
            event = objectMapper.readValue(payload, TransactionEvent.class);
        } catch (Exception ex) {
            log.error("Could not deserialize raw-transactions message: {}", ex.getMessage());
            deadLetterEventPublisher.send("unknown", "raw-transactions", payload,
                    "DESERIALIZATION_ERROR", ex.getMessage());
            return;
        }
        streamingTransactionService.processRawEvent(event, payload, "raw-transactions");
    }
}
