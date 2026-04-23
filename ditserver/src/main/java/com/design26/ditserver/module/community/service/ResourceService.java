package com.design26.ditserver.module.community.service;

import com.design26.ditserver.module.community.entity.ResourcePublicationEntity;
import com.design26.ditserver.module.community.repository.ResourcePublicationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ResourceService - 资源库（resources）功能的业务层。
 *
 * 提供对 resource_publications 表的聚合查询，用于供控制器返回前端需要的结构。
 */
@Service
public class ResourceService {
    private final ResourcePublicationRepository resourceRepository;
    private final com.design26.ditserver.module.workspace.repository.ArchiveModelAssetRepository archiveModelAssetRepository;
    private final com.design26.ditserver.module.workspace.repository.ArchiveMessageRepository archiveMessageRepository;
    private final com.design26.ditserver.module.workspace.repository.ArchiveTaskRepository archiveTaskRepository;

    public ResourceService(
        ResourcePublicationRepository resourceRepository,
        com.design26.ditserver.module.workspace.repository.ArchiveModelAssetRepository archiveModelAssetRepository,
        com.design26.ditserver.module.workspace.repository.ArchiveMessageRepository archiveMessageRepository,
        com.design26.ditserver.module.workspace.repository.ArchiveTaskRepository archiveTaskRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.archiveModelAssetRepository = archiveModelAssetRepository;
        this.archiveMessageRepository = archiveMessageRepository;
        this.archiveTaskRepository = archiveTaskRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listResources() {
        List<ResourcePublicationEntity> pubs = resourceRepository.findByIsActiveTrueOrderByUpdatedAtDesc();
        List<String> archiveIds = pubs.stream().map(item -> item.getArchive().getId()).toList();

        Map<String, java.util.List<com.design26.ditserver.module.workspace.entity.ArchiveMessageEntity>> messagesByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveMessageRepository.findByArchive_IdInOrderByCreatedAtAsc(archiveIds).stream()
                .collect(Collectors.groupingBy(item -> item.getArchive().getId()));

        Map<String, java.util.List<com.design26.ditserver.module.workspace.entity.ArchiveTaskEntity>> tasksByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveTaskRepository.findByArchive_IdInOrderByCreatedAtAsc(archiveIds).stream()
                .collect(Collectors.groupingBy(item -> item.getArchive().getId()));

        Map<String, com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity> assetByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveModelAssetRepository.findByArchiveIdIn(archiveIds).stream()
                .collect(Collectors.toMap(com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity::getArchiveId, item -> item, (a, b) -> a));

        List<Map<String, Object>> archives = pubs.stream().map(publication -> {
            com.design26.ditserver.module.workspace.entity.ArchiveEntity archive = publication.getArchive();

            java.util.List<Map<String, Object>> messageViews = Optional.ofNullable(messagesByArchive.get(archive.getId())).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "role", item.getRole(),
                    "text", item.getContent(),
                    "createdAt", item.getCreatedAt()
                ))
                .collect(Collectors.toList());

            java.util.List<Map<String, Object>> taskViews = Optional.ofNullable(tasksByArchive.get(archive.getId())).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "status", item.getStatus(),
                    "progress", item.getProgress(),
                    "prompt", item.getPrompt(),
                    "imageName", Optional.ofNullable(item.getImageName()).orElse(""),
                    "imagePreview", Optional.ofNullable(item.getImagePreview()).orElse(""),
                    "createdAt", item.getCreatedAt(),
                    "updatedAt", item.getUpdatedAt()
                ))
                .collect(Collectors.toList());

            Map<String, Object> archiveView = new HashMap<>();
            archiveView.put("id", publication.getId());
            archiveView.put("communityId", publication.getId());
            archiveView.put("sourceArchiveId", archive.getId());
            archiveView.put("sourceCommunityId", publication.getId());
            archiveView.put("title", publication.getTitle());
            archiveView.put("ownerId", publication.getOwner().getId());
            archiveView.put("ownerName", publication.getOwner().getUsername());
            archiveView.put("tasks", taskViews);
            archiveView.put("messages", messageViews);
            archiveView.put("isPrivate", false);
            archiveView.put("remixCount", Optional.ofNullable(publication.getRemixCount()).orElse(0));
            archiveView.put("status", archive.getStatus());
            archiveView.put("createdAt", publication.getCreatedAt());
            archiveView.put("updatedAt", publication.getUpdatedAt());

            com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity asset = assetByArchive.get(archive.getId());
            if (asset == null) {
                archiveView.put("modelAsset", null);
            } else {
                archiveView.put("modelAsset", Map.of(
                    "id", archive.getId(),
                    "prompt", asset.getPrompt(),
                    "imagePreview", Optional.ofNullable(asset.getImagePreview()).orElse(""),
                    "createdAt", asset.getCreatedAt(),
                    "updatedAt", asset.getUpdatedAt()
                ));
            }

            return archiveView;
        }).toList();

        return Map.of("ok", true, "archives", archives);
    }
}
