package com.design26.ditserver.module.community.controller;

import com.design26.ditserver.module.community.service.FeaturedService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FeaturedController - 提供精选（featured）集合的只读 HTTP 接口。
 *
 * 端点：
 *  - GET /api/featured  返回社区中已激活的发布集合（直接复用 CommunityService 的聚合结果）。
 *
 * 设计说明：目前直接复用社区列表作为精选数据源；将来可在 service 层引入排序/筛选规则（基于 remixCount、createdAt 等）。
 */
@RestController
@RequestMapping("/api/featured")
public class FeaturedController {
    private final FeaturedService featuredService;

    public FeaturedController(FeaturedService featuredService) {
        this.featuredService = featuredService;
    }

    @GetMapping
    public Map<String, Object> listFeatured() {
        return featuredService.listFeatured();
    }
}
