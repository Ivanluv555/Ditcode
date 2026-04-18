package com.design26.ditserver.module.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "archive_model_assets")
public class ArchiveModelAssetEntity {
    @Id
    @Column(name = "archive_id", length = 64, nullable = false)
    private String archiveId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archive_id", nullable = false)
    private ArchiveEntity archive;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "image_preview", columnDefinition = "LONGTEXT")
    private String imagePreview;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public ArchiveEntity getArchive() {
        return archive;
    }

    public void setArchive(ArchiveEntity archive) {
        this.archive = archive;
        this.archiveId = archive != null ? archive.getId() : null;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImagePreview() {
        return imagePreview;
    }

    public void setImagePreview(String imagePreview) {
        this.imagePreview = imagePreview;
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
