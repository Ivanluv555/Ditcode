package com.design26.ditserver.module.auth.service;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.core.util.IdGenerator;
import com.design26.ditserver.module.auth.dto.LoginRequest;
import com.design26.ditserver.module.auth.dto.RegisterRequest;
import com.design26.ditserver.module.auth.dto.UserView;
import com.design26.ditserver.module.auth.entity.SessionEntity;
import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.auth.repository.SessionRepository;
import com.design26.ditserver.module.auth.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=]{8,32}$");
    private static final String DEFAULT_USERNAME = "demo_user";
    private static final String DEFAULT_EMAIL = "demo@ditapp.local";
    private static final String DEFAULT_PASSWORD = "demo123456";

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;

    public AuthService(
        UserRepository userRepository,
        SessionRepository sessionRepository,
        PasswordEncoder passwordEncoder,
        IdGenerator idGenerator
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Map<String, Object> defaultLogin() {
        UserEntity user = userRepository.findFirstByOrderByCreatedAtAsc().orElseGet(this::createDefaultUser);
        String token = createSession(user);
        return Map.of("ok", true, "token", token, "user", toSafeUser(user));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSessionUser(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "会话无效或已过期");
        }

        UserEntity user = sessionRepository.findByToken(token)
            .map(SessionEntity::getUser)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "会话无效或已过期"));

        return Map.of("ok", true, "user", toSafeUser(user));
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        String password = Optional.ofNullable(request.password()).orElse("");

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名格式不正确");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "邮箱格式不正确");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码格式不正确");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "邮箱已被注册");
        }

        UserEntity user = new UserEntity();
        user.setId(idGenerator.newId("user"));
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(System.currentTimeMillis());
        user = userRepository.save(user);

        String token = createSession(user);
        return Map.of("ok", true, "token", token, "user", toSafeUser(user));
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        String account = normalizeAccount(request.account());
        String password = Optional.ofNullable(request.password()).orElse("");

        UserEntity user = userRepository.findByUsernameIgnoreCase(account)
            .or(() -> userRepository.findByEmailIgnoreCase(account))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "账号不存在"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码不正确");
        }

        String token = createSession(user);
        return Map.of("ok", true, "token", token, "user", toSafeUser(user));
    }

    @Transactional
    public Map<String, Object> logout(String token) {
        if (token != null && !token.isBlank()) {
            sessionRepository.deleteByToken(token);
        }
        return Map.of("ok", true);
    }

    private UserEntity createDefaultUser() {
        UserEntity user = new UserEntity();
        user.setId(idGenerator.newId("user"));
        user.setUsername(DEFAULT_USERNAME);
        user.setEmail(DEFAULT_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setCreatedAt(System.currentTimeMillis());
        return userRepository.save(user);
    }

    private String createSession(UserEntity user) {
        sessionRepository.deleteByUser_Id(user.getId());
        SessionEntity session = new SessionEntity();
        session.setToken("mock_" + idGenerator.newId("token"));
        session.setUser(user);
        session.setCreatedAt(System.currentTimeMillis());
        session.setExpiredAt(null);
        sessionRepository.save(session);
        return session.getToken();
    }

    private UserView toSafeUser(UserEntity user) {
        return new UserView(user.getId(), user.getUsername(), user.getEmail());
    }

    private String normalizeUsername(String username) {
        return Optional.ofNullable(username).orElse("").trim();
    }

    private String normalizeEmail(String email) {
        return Optional.ofNullable(email).orElse("").trim().toLowerCase();
    }

    private String normalizeAccount(String account) {
        return Optional.ofNullable(account).orElse("").trim().toLowerCase();
    }
}
