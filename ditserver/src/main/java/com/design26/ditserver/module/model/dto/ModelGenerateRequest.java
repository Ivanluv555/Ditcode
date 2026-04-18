package com.design26.ditserver.module.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ModelGenerateRequest(
    String taskId,
    String archiveId,
    @NotBlank(message = "prompt 不能为空")
    String prompt,
    String imageName,
    @NotBlank(message = "imageBase64 不能为空")
    String imageBase64
) {
}
