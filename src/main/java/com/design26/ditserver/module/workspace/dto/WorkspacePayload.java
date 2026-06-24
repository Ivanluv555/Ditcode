package com.design26.ditserver.module.workspace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspacePayload {
    private Integer version;
    private String activeArchiveId;
    private List<String> archiveOrder;
    private Map<String, ArchivePayload> archives;
    private List<TaskPayload> tasks;
    private List<ModelAssetPayload> assetHistory;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getActiveArchiveId() {
        return activeArchiveId;
    }

    public void setActiveArchiveId(String activeArchiveId) {
        this.activeArchiveId = activeArchiveId;
    }

    public List<String> getArchiveOrder() {
        return archiveOrder;
    }

    public void setArchiveOrder(List<String> archiveOrder) {
        this.archiveOrder = archiveOrder;
    }

    public Map<String, ArchivePayload> getArchives() {
        return archives;
    }

    public void setArchives(Map<String, ArchivePayload> archives) {
        this.archives = archives;
    }

    public List<TaskPayload> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskPayload> tasks) {
        this.tasks = tasks;
    }

    public List<ModelAssetPayload> getAssetHistory() {
        return assetHistory;
    }

    public void setAssetHistory(List<ModelAssetPayload> assetHistory) {
        this.assetHistory = assetHistory;
    }
}
