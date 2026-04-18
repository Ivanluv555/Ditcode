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
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("archiveId", defaultString(request.archiveId()));
        payload.put("prompt", request.prompt().trim());
        payload.put("imageName", defaultString(request.imageName()));
        payload.put("imagePreview", defaultString(request.imagePreview()));
        payload.put("client", "ditserver");

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

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "模型服务调用失败");
            }

            Map<String, Object> raw = objectMapper.readValue(response.body(), new TypeReference<>() {});
            String imagePreview = pickImagePreview(raw, request.imagePreview());
            String provider = Optional.ofNullable(raw.get("provider")).map(Object::toString).orElse("upstream");
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
}
