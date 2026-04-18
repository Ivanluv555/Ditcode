package com.design26.ditserver.core.auth;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.module.auth.entity.SessionEntity;
import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.auth.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthContextService {
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionRepository sessionRepository;

    public AuthContextService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Optional<UserEntity> findCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByToken(token).map(SessionEntity::getUser);
    }

    public UserEntity requireCurrentUser(HttpServletRequest request) {
        return findCurrentUser(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }

    public String extractToken(HttpServletRequest request) {
        String auth = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        if (!auth.startsWith(BEARER_PREFIX)) {
            return "";
        }
        return auth.substring(BEARER_PREFIX.length()).trim();
    }
}
