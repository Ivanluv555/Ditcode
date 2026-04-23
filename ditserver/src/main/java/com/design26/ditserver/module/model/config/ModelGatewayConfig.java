package com.design26.ditserver.module.model.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelGatewayConfig - 模型网关相关的Spring配置。
 *
 * 业务角色：基于 ModelGatewayProperties 构造并暴露用于调用上游模型的 HttpClient Bean，
 * 使 ModelGatewayService 能注入并复用该客户端。
 */
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
