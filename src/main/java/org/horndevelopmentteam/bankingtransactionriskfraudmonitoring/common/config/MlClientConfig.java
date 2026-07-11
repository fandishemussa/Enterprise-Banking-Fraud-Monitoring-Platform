package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MlClientConfig {

    @Bean
    public RestClient mlServiceRestClient(
            @Value("${ml.service.url}") String mlServiceUrl,
            @Value("${ml.service.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${ml.service.read-timeout-ms}") int readTimeoutMs,
            @Value("${ml.service.api-key:}") String mlServiceApiKey) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory);

        // Matches ml-service's ML_API_KEY: blank means the ML service enforces nothing, so sending
        // an empty header is harmless either way.
        if (!mlServiceApiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", mlServiceApiKey);
        }

        return builder.build();
    }
}
