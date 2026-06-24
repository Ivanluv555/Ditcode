package com.design26.ditserver.module.workspace.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchivePayload {
    private String id;
    private String title;
    private String ownerId;
    private String ownerName;
    private List<TaskPayload> tasks;
    private List<MessagePayload> messages;
    private ModelAssetPayload modelAsset;
    private String sourceCommunityId;
    private Boolean isPrivate;
    private Integer remixCount;
    private String status;
    private Long createdAt;
    private Long updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<TaskPayload> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskPayload> tasks) {
        this.tasks = tasks;
    }

    public List<MessagePayload> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagePayload> messages) {
        this.messages = messages;
    }

    public ModelAssetPayload getModelAsset() {
        return modelAsset;
    }

    public void setModelAsset(ModelAssetPayload modelAsset) {
        this.modelAsset = modelAsset;
    }

    public String getSourceCommunityId() {
        return sourceCommunityId;
    }

    public void setSourceCommunityId(String sourceCommunityId) {
        this.sourceCommunityId = sourceCommunityId;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Integer getRemixCount() {
        return remixCount;
    }

    public void setRemixCount(Integer remixCount) {
        this.remixCount = remixCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
