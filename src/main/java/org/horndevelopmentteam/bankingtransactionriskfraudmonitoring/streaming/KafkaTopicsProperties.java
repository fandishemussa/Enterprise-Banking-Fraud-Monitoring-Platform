package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.streaming;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topics")
@Getter
@Setter
public class KafkaTopicsProperties {
    private String rawTransactions;
    private String scoredTransactions;
    private String fraudAlerts;
    private String deadLetterTransactions;
}
