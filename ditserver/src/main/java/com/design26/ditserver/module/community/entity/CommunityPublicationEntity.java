package com.design26.ditserver.module.community.entity;

import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "community_publications")
/**
 * CommunityPublicationEntity - 社区发布（publication）持久化实体。
 *
 * 业务角色：映射 community_publications 表，表示用户将某个 Archive 发布到社区后生成的记录，
 * 存储标题、发布者、激活状态和 remix 统计等信息，供社区列表与 remix 统计使用。
 */
public class CommunityPublicationEntity {
    @Id
    @Column(length = 128, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archive_id", nullable = false, unique = true)
    private ArchiveEntity archive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(length = 128, nullable = false)
    private String title;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "remix_count", nullable = false)
    private Integer remixCount;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArchiveEntity getArchive() {
        return archive;
    }

    public void setArchive(ArchiveEntity archive) {
        this.archive = archive;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getRemixCount() {
        return remixCount;
    }

    public void setRemixCount(Integer remixCount) {
        this.remixCount = remixCount;
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
