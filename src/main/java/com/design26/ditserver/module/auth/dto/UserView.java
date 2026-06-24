package com.design26.ditserver.module.auth.dto;

public record UserView(
    String id,
    String username,
    String email
) {
}
