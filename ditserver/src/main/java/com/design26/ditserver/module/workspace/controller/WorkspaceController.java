package com.design26.ditserver.module.workspace.controller;

import com.design26.ditserver.core.auth.AuthContextService;
import com.design26.ditserver.module.workspace.dto.WorkspacePayload;
import com.design26.ditserver.module.workspace.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final AuthContextService authContextService;
    private final WorkspaceService workspaceService;

    public WorkspaceController(AuthContextService authContextService, WorkspaceService workspaceService) {
        this.authContextService = authContextService;
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public Map<String, Object> getWorkspace(HttpServletRequest request) {
        return workspaceService.getWorkspace(authContextService.requireCurrentUser(request));
    }

    @PutMapping
    public Map<String, Object> putWorkspace(HttpServletRequest request, @RequestBody WorkspacePayload payload) {
        return workspaceService.putWorkspace(authContextService.requireCurrentUser(request), payload);
    }

    @PostMapping("/reset")
    public Map<String, Object> resetWorkspace(HttpServletRequest request) {
        return workspaceService.resetWorkspace(authContextService.requireCurrentUser(request));
    }
}
