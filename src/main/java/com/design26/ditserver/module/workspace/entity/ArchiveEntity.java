package com.design26.ditserver.module.workspace.entity;

import com.design26.ditserver.module.auth.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "archives")
/**
 * ArchiveEntity - 用户工作区（存档）持久化实体。
 *
 * 业务角色：映射 archives 表，保存用户的工作数据集（标题、状态、是否私有、来源引用等），
 * 是 Workspace 功能的核心数据结构，关联消息、任务与模型资产实体。
 */
public class ArchiveEntity {
    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(length = 128, nullable = false)
    private String title;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_archive_id")
    private ArchiveEntity sourceArchive;

    @Column(name = "source_community_id", length = 128)
    private String sourceCommunityId;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "deleted_at")
    private Long deletedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public ArchiveEntity getSourceArchive() {
        return sourceArchive;
    }

    public void setSourceArchive(ArchiveEntity sourceArchive) {
        this.sourceArchive = sourceArchive;
    }

    public String getSourceCommunityId() {
        return sourceCommunityId;
    }

    public void setSourceCommunityId(String sourceCommunityId) {
        this.sourceCommunityId = sourceCommunityId;
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

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
