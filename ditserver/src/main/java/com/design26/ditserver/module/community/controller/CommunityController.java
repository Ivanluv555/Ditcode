package com.design26.ditserver.module.community.controller;

import com.design26.ditserver.core.auth.AuthContextService;
import com.design26.ditserver.module.community.dto.ArchiveIdRequest;
import com.design26.ditserver.module.community.dto.CommunityRemixRequest;
import com.design26.ditserver.module.community.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CommunityController - 社区相关的HTTP接口层。
 *
 * 业务角色：提供社区存档的查询（列表）、发布/取消发布和 remix 操作的REST接口，
 * 将请求委派给 CommunityService，并使用 AuthContextService 验证用户权限。
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final AuthContextService authContextService;
    private final CommunityService communityService;

    public CommunityController(AuthContextService authContextService, CommunityService communityService) {
        this.authContextService = authContextService;
        this.communityService = communityService;
    }

    @GetMapping
    public Map<String, Object> list() {
        return communityService.listCommunityArchives();
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(HttpServletRequest request, @Valid @RequestBody ArchiveIdRequest body) {
        return communityService.publishArchive(authContextService.requireCurrentUser(request), body.archiveId().trim());
    }

    @PostMapping("/unpublish")
    public Map<String, Object> unpublish(HttpServletRequest request, @Valid @RequestBody ArchiveIdRequest body) {
        return communityService.unpublishArchive(authContextService.requireCurrentUser(request), body.archiveId().trim());
    }

    @PostMapping("/remix")
    public Map<String, Object> remix(HttpServletRequest request, @Valid @RequestBody CommunityRemixRequest body) {
        return communityService.recordRemix(authContextService.requireCurrentUser(request), body.communityId().trim());
    }
}
