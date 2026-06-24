package com.design26.ditserver.module.model.dto;

import java.util.Map;

/**
 * ModelGenerateResponse - 模型生成结果的不可变DTO。
 *
 * 业务角色：将上游模型（或本地回退）返回的结果标准化为内部格式，包含是否成功、提供者标识、
 * 任务ID、预览图/URL、完成时间与原始响应（raw），供控制器直接转为API响应。
 */
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
