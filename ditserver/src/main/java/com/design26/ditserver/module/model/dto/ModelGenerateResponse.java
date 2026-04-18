package com.design26.ditserver.module.model.dto;

import java.util.Map;

public record ModelGenerateResponse(
    boolean ok,
    String provider,
    String taskId,
    String prompt,
    String imagePreview,
    Long finishedAt,
    Map<String, Object> raw
) {
}
