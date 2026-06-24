package com.design26.ditserver.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    String username,
    @NotBlank(message = "邮箱不能为空")
    String email,
    @NotBlank(message = "密码不能为空")
    String password
) {
}
