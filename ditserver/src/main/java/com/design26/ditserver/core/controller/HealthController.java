package com.design26.ditserver.core.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 健康检查控制器，提供一个简单的接口来检查服务是否正常运行
@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "service", "ditapp-spring-server");
    }
}
