package com.design26.ditserver.module.community.controller;

import com.design26.ditserver.module.community.service.ResourceService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ResourcesController - 提供资源库（resources）集合的 HTTP 接口。
 *
 * 端点：
 *  - GET /api/resources  返回用于资源灵感库的条目集合（当前实现复用 community 列表作为资源）。
 *
 * 说明：资源条目可能需要 category、tags 等字段；当前仓库模型未包含这些字段，因此先返回社区发布的聚合结果，
 * 前端可据此展示 resource 列表。后续可在 CommunityService 中增加针对资源库的专门查询。
 */
@RestController
@RequestMapping("/api/resources")
public class ResourcesController {
    private final ResourceService resourceService;

    public ResourcesController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public Map<String, Object> listResources() {
        return resourceService.listResources();
    }
}
