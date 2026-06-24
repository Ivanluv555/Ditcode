package com.design26.ditserver.module.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ModelGenerateRequest - 模型生成请求参数封装（不可变record）。
 *
 * 业务角色：作为控制器接收的输入DTO，携带调用模型所需的 prompt、可选的图像数据以及关联ID，
 * 并由验证注解保证必要字段的正确性。
 */
public record ModelGenerateRequest(
    String taskId,
    String archiveId,
    @NotBlank(message = "prompt 不能为空")
    String prompt,
    String imageName,
    String imageBase64
) {
}
