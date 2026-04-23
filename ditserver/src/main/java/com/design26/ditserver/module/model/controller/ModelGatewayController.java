package com.design26.ditserver.module.model.controller;

import com.design26.ditserver.module.model.dto.ModelGenerateRequest;
import com.design26.ditserver.module.model.dto.ModelGenerateResponse;
import com.design26.ditserver.module.model.service.ModelGatewayService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ModelGatewayController - 模型调用对外入口控制器。
 *
 * 业务角色：接收前端关于模型生成的请求（如图片/文本生成），将请求委派给 ModelGatewayService，
 * 并把服务返回的数据按API契约格式化为JSON返回给客户端。
 */
@RestController
@RequestMapping("/api/model")
public class ModelGatewayController {
    private final ModelGatewayService modelGatewayService;

    public ModelGatewayController(ModelGatewayService modelGatewayService) {
        this.modelGatewayService = modelGatewayService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@Valid @RequestBody ModelGenerateRequest request) {
        ModelGenerateResponse result = modelGatewayService.generate(request);
        return Map.of(
            "ok", result.ok(),
            "provider", result.provider(),
            "taskId", result.taskId(),
            "prompt", result.prompt(),
            "imagePreview", result.imagePreview(),
            "finishedAt", result.finishedAt(),
            "raw", result.raw()
        );
    }
}
