package com.design26.ditserver.module.workspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "archive_tasks")
public class ArchiveTaskEntity {
    @Id
    @Column(length = 96, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "archive_id", nullable = false)
    private ArchiveEntity archive;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer progress;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "image_name", length = 255)
    private String imageName;

    @Column(name = "image_preview", columnDefinition = "LONGTEXT")
    private String imagePreview;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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
