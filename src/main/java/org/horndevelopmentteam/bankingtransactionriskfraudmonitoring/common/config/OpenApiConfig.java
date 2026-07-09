package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudMonitoringOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Enterprise Banking Fraud Monitoring Platform API")
                .description("Transaction ingestion, rule-based risk scoring, fraud alerts, and investigation workflows")
                .version("v1.0.0"));
    }
}
