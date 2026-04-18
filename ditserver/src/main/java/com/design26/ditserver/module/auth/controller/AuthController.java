package com.design26.ditserver.module.auth.controller;

import com.design26.ditserver.core.auth.AuthContextService;
import com.design26.ditserver.module.auth.dto.LoginRequest;
import com.design26.ditserver.module.auth.dto.RegisterRequest;
import com.design26.ditserver.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthContextService authContextService;

    public AuthController(AuthService authService, AuthContextService authContextService) {
        this.authService = authService;
        this.authContextService = authContextService;
    }

    @PostMapping("/default-login")
    public Map<String, Object> defaultLogin() {
        return authService.defaultLogin();
    }

    @GetMapping("/session")
    public Map<String, Object> session(HttpServletRequest request) {
        return authService.getSessionUser(authContextService.extractToken(request));
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        return authService.logout(authContextService.extractToken(request));
    }
}
