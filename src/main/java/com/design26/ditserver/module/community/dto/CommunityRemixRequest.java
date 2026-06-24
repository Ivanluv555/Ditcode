package com.design26.ditserver.module.community.dto;

import jakarta.validation.constraints.NotBlank;

public record CommunityRemixRequest(
    @NotBlank(message = "communityId is required")
    String communityId
) {
}
