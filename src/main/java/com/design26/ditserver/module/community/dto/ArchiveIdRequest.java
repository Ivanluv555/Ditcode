package com.design26.ditserver.module.community.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchiveIdRequest(
    @NotBlank(message = "archiveId is required")
    String archiveId
) {
}
