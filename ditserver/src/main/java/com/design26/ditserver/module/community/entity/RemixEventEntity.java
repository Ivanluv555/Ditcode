package com.design26.ditserver.module.community.entity;

import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "remix_events")
/**
 * RemixEventEntity - 记录 remix 行为的实体。
 *
 * 业务角色：当用户基于社区存档进行 remix 时记录事件（谁 remix、来源存档、目标存档、时间），
 * 供社区统计和回溯使用。
 */
public class RemixEventEntity {
    @Id
    @Column(length = 128, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private CommunityPublicationEntity publication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remixer_archive_id", nullable = false)
    private ArchiveEntity remixerArchive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remixer_user_id", nullable = false)
    private UserEntity remixerUser;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CommunityPublicationEntity getPublication() {
        return publication;
    }

    public void setPublication(CommunityPublicationEntity publication) {
        this.publication = publication;
    }

    public ArchiveEntity getRemixerArchive() {
        return remixerArchive;
    }

    public void setRemixerArchive(ArchiveEntity remixerArchive) {
        this.remixerArchive = remixerArchive;
    }

    public UserEntity getRemixerUser() {
        return remixerUser;
    }

    public void setRemixerUser(UserEntity remixerUser) {
        this.remixerUser = remixerUser;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
