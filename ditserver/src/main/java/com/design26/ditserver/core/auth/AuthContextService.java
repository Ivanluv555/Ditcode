package com.design26.ditserver.core.auth;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.module.auth.entity.SessionEntity;
import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.auth.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


/* 
 * 认证上下文服务
 * 负责从HTTP请求中提取认证信息，并根据会话信息查询当前用户
 * 提供获取当前用户的接口，供控制器等其他组件使用
 */
/**
 * AuthContextService - 认证上下文与会话提取工具服务。
 *
 * 业务角色：从 HttpServletRequest 中解析 Authorization Token，并通过 SessionRepository
 * 查找并返回当前会话对应的 UserEntity。供控制器和服务用于获取当前用户或要求登录。
 */
@Service
public class AuthContextService {
    // Bearer Token的前缀，一会儿给它剪掉
    private static final String BEARER_PREFIX = "Bearer ";

    // 会话仓库，用于查询会话信息和关联的用户信息
    private final SessionRepository sessionRepository;

    // 构造函数，注入会话仓库
    public AuthContextService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // 从HTTP请求中查找当前用户，如果没有找到或没有提供有效的Token，则返回空的Optional
    public Optional<UserEntity> findCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByToken(token).map(SessionEntity::getUser);
    }

    // 从HTTP请求中获取当前用户，如果没有找到或没有提供有效的Token，则抛出401未授权异常
    public UserEntity requireCurrentUser(HttpServletRequest request) {
        return findCurrentUser(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }

    // 从HTTP请求的Authorization头中提取Bearer Token，如果没有提供或格式不正确，则返回空字符串
    public String extractToken(HttpServletRequest request) {
        String auth = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        if (!auth.startsWith(BEARER_PREFIX)) {
            return "";
        }
        return auth.substring(BEARER_PREFIX.length()).trim();
    }
}
