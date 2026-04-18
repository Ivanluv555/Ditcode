package com.design26.ditserver.module.community.service;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.core.util.IdGenerator;
import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.community.entity.CommunityPublicationEntity;
import com.design26.ditserver.module.community.entity.RemixEventEntity;
import com.design26.ditserver.module.community.repository.CommunityPublicationRepository;
import com.design26.ditserver.module.community.repository.RemixEventRepository;
import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveMessageEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveTaskEntity;
import com.design26.ditserver.module.workspace.repository.ArchiveMessageRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveModelAssetRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveTaskRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityService {
    private final CommunityPublicationRepository communityPublicationRepository;
    private final RemixEventRepository remixEventRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchiveMessageRepository archiveMessageRepository;
    private final ArchiveTaskRepository archiveTaskRepository;
    private final ArchiveModelAssetRepository archiveModelAssetRepository;
    private final IdGenerator idGenerator;

    public CommunityService(
        CommunityPublicationRepository communityPublicationRepository,
        RemixEventRepository remixEventRepository,
        ArchiveRepository archiveRepository,
        ArchiveMessageRepository archiveMessageRepository,
        ArchiveTaskRepository archiveTaskRepository,
        ArchiveModelAssetRepository archiveModelAssetRepository,
        IdGenerator idGenerator
    ) {
        this.communityPublicationRepository = communityPublicationRepository;
        this.remixEventRepository = remixEventRepository;
        this.archiveRepository = archiveRepository;
        this.archiveMessageRepository = archiveMessageRepository;
        this.archiveTaskRepository = archiveTaskRepository;
        this.archiveModelAssetRepository = archiveModelAssetRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listCommunityArchives() {
        List<CommunityPublicationEntity> publications = communityPublicationRepository.findByIsActiveTrueOrderByUpdatedAtDesc();
        List<String> archiveIds = publications.stream().map(item -> item.getArchive().getId()).toList();

        Map<String, List<ArchiveMessageEntity>> messagesByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveMessageRepository.findByArchive_IdInOrderByCreatedAtAsc(archiveIds).stream()
                .collect(Collectors.groupingBy(item -> item.getArchive().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<String, List<ArchiveTaskEntity>> tasksByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveTaskRepository.findByArchive_IdInOrderByCreatedAtAsc(archiveIds).stream()
                .collect(Collectors.groupingBy(item -> item.getArchive().getId(), LinkedHashMap::new, Collectors.toList()));
        Map<String, ArchiveModelAssetEntity> assetByArchive = archiveIds.isEmpty()
            ? Map.of()
            : archiveModelAssetRepository.findByArchiveIdIn(archiveIds).stream()
                .collect(Collectors.toMap(ArchiveModelAssetEntity::getArchiveId, item -> item, (a, b) -> a));

        List<Map<String, Object>> archives = publications.stream().map(publication -> {
            ArchiveEntity archive = publication.getArchive();

            List<Map<String, Object>> messageViews = Optional.ofNullable(messagesByArchive.get(archive.getId())).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "role", item.getRole(),
                    "text", item.getContent(),
                    "createdAt", item.getCreatedAt()
                ))
                .collect(Collectors.toList());

            List<Map<String, Object>> taskViews = Optional.ofNullable(tasksByArchive.get(archive.getId())).orElse(List.of()).stream()
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

            ArchiveModelAssetEntity asset = assetByArchive.get(archive.getId());
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

    @Transactional
    public Map<String, Object> publishArchive(UserEntity user, String archiveId) {
        ArchiveEntity archive = archiveRepository.findByIdAndOwner_IdAndDeletedAtIsNull(archiveId, user.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "存档不存在或无权限"));

        long now = System.currentTimeMillis();
        CommunityPublicationEntity publication = communityPublicationRepository.findByArchive_Id(archive.getId()).orElseGet(() -> {
            CommunityPublicationEntity entity = new CommunityPublicationEntity();
            entity.setId("community_" + archive.getId());
            entity.setArchive(archive);
            entity.setOwner(user);
            entity.setCreatedAt(now);
            entity.setRemixCount(0);
            return entity;
        });

        publication.setTitle(archive.getTitle());
        publication.setOwner(user);
        publication.setIsActive(true);
        publication.setUpdatedAt(now);
        communityPublicationRepository.save(publication);

        archive.setIsPrivate(false);
        archive.setSourceCommunityId(publication.getId());
        archive.setUpdatedAt(now);
        archiveRepository.save(archive);

        return Map.of("ok", true, "isPrivate", false);
    }

    @Transactional
    public Map<String, Object> unpublishArchive(UserEntity user, String archiveId) {
        ArchiveEntity archive = archiveRepository.findByIdAndOwner_IdAndDeletedAtIsNull(archiveId, user.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "存档不存在或无权限"));

        long now = System.currentTimeMillis();
        communityPublicationRepository.findByArchive_Id(archive.getId()).ifPresent(publication -> {
            publication.setIsActive(false);
            publication.setUpdatedAt(now);
            communityPublicationRepository.save(publication);
        });

        archive.setIsPrivate(true);
        archive.setUpdatedAt(now);
        archiveRepository.save(archive);
        return Map.of("ok", true, "isPrivate", true);
    }

    @Transactional
    public Map<String, Object> recordRemix(UserEntity user, String communityId) {
        CommunityPublicationEntity publication = communityPublicationRepository.findById(communityId)
            .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "社区存档不存在"));

        int nextRemixCount = Optional.ofNullable(publication.getRemixCount()).orElse(0) + 1;
        publication.setRemixCount(nextRemixCount);
        publication.setUpdatedAt(System.currentTimeMillis());
        communityPublicationRepository.save(publication);

        archiveRepository
            .findTopByOwner_IdAndSourceCommunityIdAndDeletedAtIsNullOrderByUpdatedAtDesc(user.getId(), communityId)
            .ifPresent(remixArchive -> {
                RemixEventEntity event = new RemixEventEntity();
                event.setId(idGenerator.newId("remix"));
                event.setPublication(publication);
                event.setRemixerArchive(remixArchive);
                event.setRemixerUser(user);
                event.setCreatedAt(System.currentTimeMillis());
                remixEventRepository.save(event);
            });

        return Map.of("ok", true, "remixCount", nextRemixCount);
    }
}
