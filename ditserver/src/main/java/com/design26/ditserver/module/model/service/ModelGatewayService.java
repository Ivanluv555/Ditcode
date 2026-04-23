package com.design26.ditserver.module.model.service;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.core.util.IdGenerator;
import com.design26.ditserver.module.model.config.ModelGatewayProperties;
import com.design26.ditserver.module.model.dto.ModelGenerateRequest;
import com.design26.ditserver.module.model.dto.ModelGenerateResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ModelGatewayService - 调用/封装上游模型服务的业务实现。
 *
 * 业务角色：负责构建请求并调用外部模型网关（或回退到本地mock），
 * 解析上游响应（图片或JSON），封装成内部的 ModelGenerateResponse 以供控制器返回。
 * 处理超时、错误码和内容类型的兼容逻辑。
 */
@Service
public class ModelGatewayService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ModelGatewayProperties properties;
    private final IdGenerator idGenerator;

    public ModelGatewayService(
        HttpClient modelGatewayHttpClient,
        ObjectMapper objectMapper,
        ModelGatewayProperties properties,
        IdGenerator idGenerator
    ) {
        this.httpClient = modelGatewayHttpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.idGenerator = idGenerator;
    }

    public ModelGenerateResponse generate(ModelGenerateRequest request) {
        String taskId = Optional.ofNullable(request.taskId()).filter(item -> !item.isBlank()).orElseGet(() -> idGenerator.newId("task"));
        String upstreamUrl = Optional.ofNullable(properties.getUpstreamUrl()).orElse("").trim();

        if (upstreamUrl.isBlank()) {
            return fallbackMock(request, taskId);
        }

        // 使用LinkedHashMap以获得最近的记录
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("prompt", request.prompt().trim());
        payload.put("imageBase64", normalizeImageBase64(request.imageBase64()));

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(upstreamUrl))
                .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));

            String apiKey = Optional.ofNullable(properties.getApiKey()).orElse("").trim();
            if (!apiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + apiKey);
            }

            HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "模型服务调用失败");
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            String imagePreview;
            Map<String, Object> raw;
            String provider = "upstream";

            if (contentType.startsWith("image/")) {
                String normalizedContentType = contentType.split(";")[0].trim();
                String base64 = Base64.getEncoder().encodeToString(response.body());
                imagePreview = "data:" + normalizedContentType + ";base64," + base64;
                raw = new LinkedHashMap<>();
                raw.put("provider", "upstream-image");
                raw.put("contentType", normalizedContentType);
                raw.put("xTaskId", response.headers().firstValue("X-Task-Id").orElse(""));
                raw.put("xResultMode", response.headers().firstValue("X-Result-Mode").orElse(""));
                raw.put("size", response.body().length);
                provider = "upstream-image";
            } else if (contentType.startsWith("application/json")) {
                raw = objectMapper.readValue(new String(response.body(), StandardCharsets.UTF_8), new TypeReference<>() {});
                imagePreview = pickImagePreview(raw, "");
                provider = Optional.ofNullable(raw.get("provider")).map(Object::toString).orElse("upstream");
            } else {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "模型响应格式不支持");
            }

            return new ModelGenerateResponse(
                true,
                provider,
                taskId,
                request.prompt().trim(),
                imagePreview,
                System.currentTimeMillis(),
                raw
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "模型服务不可用");
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "模型服务不可用");
        }
    }

    private ModelGenerateResponse fallbackMock(ModelGenerateRequest request, String taskId) {
        String seed = taskId.replaceAll("[^a-zA-Z0-9]", "");
        String dynamicPreview = "https://picsum.photos/seed/" + seed + "/960/540";
        Map<String, Object> raw = Map.of(
            "provider", "fallback-mock",
            "seed", seed,
            "note", "MODEL_UPSTREAM_URL 未配置，已使用动态占位资源"
        );
        return new ModelGenerateResponse(
            true,
            "fallback-mock",
            taskId,
            request.prompt().trim(),
            dynamicPreview,
            System.currentTimeMillis(),
            raw
        );
    }

    private String pickImagePreview(Map<String, Object> raw, String fallback) {
        return Optional.ofNullable(raw.get("imagePreview"))
            .or(() -> Optional.ofNullable(raw.get("imageUrl")))
            .or(() -> Optional.ofNullable(raw.get("url")))
            .map(Object::toString)
            .filter(item -> !item.isBlank())
            .orElse(defaultString(fallback));
    }

    private String defaultString(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    private String normalizeImageBase64(String value) {
        String normalized = defaultString(value).trim();
        int commaIndex = normalized.indexOf(',');
        if (normalized.startsWith("data:") && commaIndex >= 0 && commaIndex < normalized.length() - 1) {
            return normalized.substring(commaIndex + 1);
        }
        return normalized;
    }
}
