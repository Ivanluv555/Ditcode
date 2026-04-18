package com.design26.ditserver.module.model.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ModelGatewayProperties.class)
public class ModelGatewayConfig {

    @Bean
    HttpClient modelGatewayHttpClient(ModelGatewayProperties properties) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
            .build();
    }
}
