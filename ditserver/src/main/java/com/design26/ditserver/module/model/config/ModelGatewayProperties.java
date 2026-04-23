package com.design26.ditserver.module.model.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ModelGatewayProperties - 模型网关配置属性绑定类。
 *
 * 业务角色：从配置中心或环境中读取模型网关的URL、API Key及超时等配置，
 * 为 ModelGatewayService 和其 HttpClient 提供运行时参数。
 */
@ConfigurationProperties(prefix = "model.gateway")
public class ModelGatewayProperties {
    private String upstreamUrl;
    private String apiKey;
    private int connectTimeoutMs = 10000;
    private int readTimeoutMs = 2000000;

    public String getUpstreamUrl() {
        return upstreamUrl;
    }

    public void setUpstreamUrl(String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
