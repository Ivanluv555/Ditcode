package com.design26.ditserver.core.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController - 服务健康检查接口。
 *
 * 业务角色：对外提供轻量的 /api/health 接口用于监控与就绪/存活检查，方便部署与运维对服务可用性进行探测。
 */
@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "service", "ditapp-spring-server");
    }
}
