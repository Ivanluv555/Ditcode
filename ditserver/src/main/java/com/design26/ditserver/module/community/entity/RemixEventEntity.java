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
