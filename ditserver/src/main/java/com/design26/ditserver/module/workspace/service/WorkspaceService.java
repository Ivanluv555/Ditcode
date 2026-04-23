package com.design26.ditserver.module.workspace.service;

import com.design26.ditserver.core.error.ApiException;
import com.design26.ditserver.core.util.IdGenerator;
import com.design26.ditserver.module.auth.entity.UserEntity;
import com.design26.ditserver.module.community.entity.CommunityPublicationEntity;
import com.design26.ditserver.module.community.repository.CommunityPublicationRepository;
import com.design26.ditserver.module.community.repository.RemixEventRepository;
import com.design26.ditserver.module.workspace.dto.ArchivePayload;
import com.design26.ditserver.module.workspace.dto.MessagePayload;
import com.design26.ditserver.module.workspace.dto.ModelAssetPayload;
import com.design26.ditserver.module.workspace.dto.TaskPayload;
import com.design26.ditserver.module.workspace.dto.WorkspacePayload;
import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveMessageEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity;
import com.design26.ditserver.module.workspace.entity.ArchiveTaskEntity;
import com.design26.ditserver.module.workspace.repository.ArchiveMessageRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveModelAssetRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveRepository;
import com.design26.ditserver.module.workspace.repository.ArchiveTaskRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WorkspaceService - 工作区（Archive）核心业务实现。
 *
 * 业务角色：负责用户工作区的构建、持久化、任务与消息的管理、以及与社区发布/Remix 的协同逻辑，
 * 包含一系列事务方法来保证 Archive 及其子实体（messages/tasks/modelAsset）的数据一致性。
 */
@Service
public class WorkspaceService {
    private static final Set<String> ARCHIVE_STATUS = Set.of("idle", "inferencing", "success", "failed");
    private static final Set<String> TASK_STATUS = Set.of("queued", "inferencing", "success", "failed");
    private static final Set<String> MESSAGE_ROLE = Set.of("user", "assistant", "system");

    private final ArchiveRepository archiveRepository;
    private final ArchiveMessageRepository archiveMessageRepository;
    private final ArchiveTaskRepository archiveTaskRepository;
    private final ArchiveModelAssetRepository archiveModelAssetRepository;
    private final CommunityPublicationRepository communityPublicationRepository;
    private final RemixEventRepository remixEventRepository;
    private final IdGenerator idGenerator;

    public WorkspaceService(
        ArchiveRepository archiveRepository,
        ArchiveMessageRepository archiveMessageRepository,
        ArchiveTaskRepository archiveTaskRepository,
        ArchiveModelAssetRepository archiveModelAssetRepository,
        CommunityPublicationRepository communityPublicationRepository,
        RemixEventRepository remixEventRepository,
        IdGenerator idGenerator
    ) {
        this.archiveRepository = archiveRepository;
        this.archiveMessageRepository = archiveMessageRepository;
        this.archiveTaskRepository = archiveTaskRepository;
        this.archiveModelAssetRepository = archiveModelAssetRepository;
        this.communityPublicationRepository = communityPublicationRepository;
        this.remixEventRepository = remixEventRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWorkspace(UserEntity user) {
        return buildWorkspaceResponse(user);
    }

    @Transactional
    public Map<String, Object> putWorkspace(UserEntity user, WorkspacePayload payload) {
        Map<String, ArchivePayload> archivesPayload = Optional.ofNullable(payload.getArchives()).orElse(Map.of());
        Set<String> incomingIds = new HashSet<>();

        List<ArchiveEntity> existing = archiveRepository.findByOwner_IdAndDeletedAtIsNullOrderByUpdatedAtDesc(user.getId());
        Map<String, ArchiveEntity> existingById = existing.stream()
            .collect(Collectors.toMap(ArchiveEntity::getId, item -> item, (a, b) -> a));

        for (Map.Entry<String, ArchivePayload> entry : archivesPayload.entrySet()) {
            String fallbackId = safeText(entry.getKey());
            ArchivePayload archivePayload = Optional.ofNullable(entry.getValue()).orElse(new ArchivePayload());

            String archiveId = safeText(archivePayload.getId());
            if (archiveId.isBlank()) {
                archiveId = fallbackId;
            }
            if (archiveId.isBlank()) {
                archiveId = idGenerator.newId("archive");
            }

            incomingIds.add(archiveId);
            ArchiveEntity archive = existingById.getOrDefault(archiveId, new ArchiveEntity());
            archive.setId(archiveId);
            archive.setOwner(user);
            archive.setTitle(defaultIfBlank(archivePayload.getTitle(), "新工作"));
            archive.setStatus(normalizeArchiveStatus(archivePayload.getStatus()));
            archive.setIsPrivate(archivePayload.getIsPrivate() == null || archivePayload.getIsPrivate());
            archive.setSourceCommunityId(safeText(archivePayload.getSourceCommunityId()));
            archive.setSourceArchive(resolveSourceArchive(archivePayload, existingById));

            long now = System.currentTimeMillis();
            archive.setCreatedAt(archivePayload.getCreatedAt() != null ? archivePayload.getCreatedAt() : Optional.ofNullable(archive.getCreatedAt()).orElse(now));
            archive.setUpdatedAt(archivePayload.getUpdatedAt() != null ? archivePayload.getUpdatedAt() : now);
            archive.setDeletedAt(null);
            archive = archiveRepository.save(archive);

            replaceArchiveChildren(archive, archivePayload);
        }

        for (ArchiveEntity archive : existing) {
            if (!incomingIds.contains(archive.getId())) {
                deleteArchiveWithDependencies(archive.getId());
            }
        }

        return buildWorkspaceResponse(user);
    }

    @Transactional
    public Map<String, Object> resetWorkspace(UserEntity user) {
        List<ArchiveEntity> existing = archiveRepository.findByOwner_IdAndDeletedAtIsNullOrderByUpdatedAtDesc(user.getId());
        for (ArchiveEntity archive : existing) {
            deleteArchiveWithDependencies(archive.getId());
        }
        return buildWorkspaceResponse(user);
    }

    private ArchiveEntity resolveSourceArchive(ArchivePayload payload, Map<String, ArchiveEntity> existingById) {
        String source = safeText(payload.getSourceCommunityId());
        if (source.isBlank()) {
            return null;
        }
        // source_archive_id is optional in v1; current frontend tracks sourceCommunityId only.
        return existingById.values().stream()
            .filter(item -> Objects.equals(item.getSourceCommunityId(), source))
            .findFirst()
            .orElse(null);
    }
/*
    private void replaceArchiveChildren(ArchiveEntity archive, ArchivePayload payload) {
        archiveMessageRepository.deleteByArchive_Id(archive.getId());
        archiveTaskRepository.deleteByArchive_Id(archive.getId());

        List<MessagePayload> messages = Optional.ofNullable(payload.getMessages()).orElse(List.of());
        List<TaskPayload> tasks = Optional.ofNullable(payload.getTasks()).orElse(List.of());
        ModelAssetPayload modelAsset = payload.getModelAsset();

        long now = System.currentTimeMillis();
        int imageTaskCount = 0;

        for (MessagePayload messagePayload : messages) {
            String content = safeText(messagePayload.getText());
            String imagePreview = safeText(messagePayload.getImagePreview());
            if (content.isBlank() && imagePreview.isBlank()) {
                continue;
            }
            ArchiveMessageEntity message = new ArchiveMessageEntity();
            message.setId(defaultIfBlank(messagePayload.getId(), idGenerator.newId("msg")));
            message.setArchive(archive);
            message.setRole(normalizeMessageRole(messagePayload.getRole()));
            message.setContent(content);
            message.setImagePreview(blankToNull(imagePreview));
            message.setCreatedAt(messagePayload.getCreatedAt() != null ? messagePayload.getCreatedAt() : now);
            archiveMessageRepository.save(message);
        }

        for (TaskPayload taskPayload : tasks) {
            ArchiveTaskEntity task = new ArchiveTaskEntity();
            task.setId(defaultIfBlank(taskPayload.getId(), idGenerator.newId("task")));
            task.setArchive(archive);
            task.setStatus(normalizeTaskStatus(taskPayload.getStatus()));
            task.setProgress(clamp(taskPayload.getProgress(), 0, 100));
            task.setPrompt(defaultIfBlank(taskPayload.getPrompt(), ""));
            task.setImageName(blankToNull(taskPayload.getImageName()));
            task.setImagePreview(normalizeTaskImagePreview(taskPayload.getImagePreview()));
            if (task.getImageName() != null || task.getImagePreview() != null) {
                imageTaskCount++;
            }
            task.setCreatedAt(taskPayload.getCreatedAt() != null ? taskPayload.getCreatedAt() : now);
            task.setUpdatedAt(taskPayload.getUpdatedAt() != null ? taskPayload.getUpdatedAt() : task.getCreatedAt());
            archiveTaskRepository.save(task);
        }

        //if (imageTaskCount > 1) {
        //    throw new ApiException(HttpStatus.BAD_REQUEST, "每个存档仅首轮任务允许图片");
        //}

        // ✅ 替换为这段“平滑更新”逻辑
        if (modelAsset != null) {
            if (archive.getId() == null || archive.getId().isBlank()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "存档ID为空，无法保存模型资源");
            }

            // 🌟 核心修复：先从数据库查，如果有就复用老对象更新字段；如果没有才 new 一个新的。
            // 这样完美避开了 Hibernate 的“同 ID 删除再新建”的缓存崩溃 Bug。
            ArchiveModelAssetEntity asset = archiveModelAssetRepository.findById(archive.getId())
                    .orElseGet(() -> {
                        ArchiveModelAssetEntity newAsset = new ArchiveModelAssetEntity();
                        newAsset.setArchive(archive);
                        return newAsset;
                    });

            asset.setPrompt(defaultIfBlank(modelAsset.getPrompt(), ""));
            // 这里把前端传来的 "/api/assets/xxx.jpg" 存入
            asset.setImagePreview(blankToNull(modelAsset.getImagePreview()));
            asset.setCreatedAt(modelAsset.getCreatedAt() != null ? modelAsset.getCreatedAt() : now);
            asset.setUpdatedAt(modelAsset.getUpdatedAt() != null ? modelAsset.getUpdatedAt() : asset.getCreatedAt());

            archiveModelAssetRepository.save(asset);
        } else {
            // 只有当前端真的传来空的 asset 时，才执行删除清理
            archiveModelAssetRepository.deleteById(archive.getId());
        }
    }

    private Map<String, Object> buildWorkspaceResponse(UserEntity user) {
        List<ArchiveEntity> archives = archiveRepository.findByOwner_IdAndDeletedAtIsNullOrderByUpdatedAtDesc(user.getId());
        List<String> archiveIds = archives.stream().map(ArchiveEntity::getId).toList();

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

        Map<String, CommunityPublicationEntity> publicationByArchive = new HashMap<>();
        for (ArchiveEntity archive : archives) {
            communityPublicationRepository.findByArchive_Id(archive.getId())
                .ifPresent(publication -> publicationByArchive.put(archive.getId(), publication));
        }

        Map<String, Object> archiveMap = new LinkedHashMap<>();
        for (ArchiveEntity archive : archives) {
            List<Map<String, Object>> messageViews = Optional.ofNullable(messagesByArchive.get(archive.getId())).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "role", item.getRole(),
                    "text", item.getContent(),
                    "imagePreview", defaultIfBlank(item.getImagePreview(), ""),
                    "createdAt", item.getCreatedAt()
                ))
                .collect(Collectors.toList());

            List<Map<String, Object>> taskViews = Optional.ofNullable(tasksByArchive.get(archive.getId())).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "status", item.getStatus(),
                    "progress", item.getProgress(),
                    "prompt", item.getPrompt(),
                    "imageName", defaultIfBlank(item.getImageName(), ""),
                    "imagePreview", defaultIfBlank(item.getImagePreview(), ""),
                    "createdAt", item.getCreatedAt(),
                    "updatedAt", item.getUpdatedAt()
                ))
                .collect(Collectors.toList());

            ArchiveModelAssetEntity asset = assetByArchive.get(archive.getId());
            Map<String, Object> assetView = null;
            if (asset != null) {
                assetView = Map.of(
                    "id", archive.getId(),
                    "prompt", asset.getPrompt(),
                    "imagePreview", defaultIfBlank(asset.getImagePreview(), ""),
                    "createdAt", asset.getCreatedAt(),
                    "updatedAt", asset.getUpdatedAt()
                );
            }

            int remixCount = Optional.ofNullable(publicationByArchive.get(archive.getId()))
                .map(CommunityPublicationEntity::getRemixCount)
                .orElse(0);

            Map<String, Object> archiveView = new LinkedHashMap<>();
            archiveView.put("id", archive.getId());
            archiveView.put("title", archive.getTitle());
            archiveView.put("ownerId", archive.getOwner().getId());
            archiveView.put("ownerName", archive.getOwner().getUsername());
            archiveView.put("tasks", taskViews);
            archiveView.put("messages", messageViews);
            archiveView.put("modelAsset", assetView);
            archiveView.put("sourceCommunityId", defaultIfBlank(archive.getSourceCommunityId(), ""));
            archiveView.put("isPrivate", archive.getIsPrivate());
            archiveView.put("remixCount", remixCount);
            archiveView.put("status", archive.getStatus());
            archiveView.put("createdAt", archive.getCreatedAt());
            archiveView.put("updatedAt", archive.getUpdatedAt());

            archiveMap.put(archive.getId(), archiveView);
        }

        List<String> archiveOrder = new ArrayList<>(archiveMap.keySet());
        String activeArchiveId = archiveOrder.isEmpty() ? "" : archiveOrder.getFirst();

        List<Map<String, Object>> activeTasks = activeArchiveId.isBlank()
            ? List.of()
            : Optional.ofNullable(tasksByArchive.get(activeArchiveId)).orElse(List.of()).stream()
                .map(item -> Map.<String, Object>of(
                    "id", item.getId(),
                    "status", item.getStatus(),
                    "progress", item.getProgress(),
                    "prompt", item.getPrompt(),
                    "imageName", defaultIfBlank(item.getImageName(), ""),
                    "imagePreview", defaultIfBlank(item.getImagePreview(), ""),
                    "createdAt", item.getCreatedAt(),
                    "updatedAt", item.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        List<Map<String, Object>> activeAssetHistory = new ArrayList<>();
        if (!activeArchiveId.isBlank()) {
            ArchiveModelAssetEntity asset = assetByArchive.get(activeArchiveId);
            if (asset != null) {
                activeAssetHistory.add(Map.of(
                    "id", activeArchiveId,
                    "prompt", asset.getPrompt(),
                    "imagePreview", defaultIfBlank(asset.getImagePreview(), ""),
                    "createdAt", asset.getCreatedAt(),
                    "updatedAt", asset.getUpdatedAt()
                ));
            }
        }

        return Map.of(
            "ok", true,
            "version", 2,
            "activeArchiveId", activeArchiveId,
            "archiveOrder", archiveOrder,
            "archives", archiveMap,
            "tasks", activeTasks,
            "assetHistory", activeAssetHistory
        );
    }
*/
private void replaceArchiveChildren(ArchiveEntity archive, ArchivePayload payload) {
    System.out.println("\n============= [数据追踪钩子: 开始处理工作区保存] =============");
    System.out.println(">> 当前存档 ID: " + archive.getId());

    archiveMessageRepository.deleteByArchive_Id(archive.getId());
    archiveTaskRepository.deleteByArchive_Id(archive.getId());

    List<MessagePayload> messages = Optional.ofNullable(payload.getMessages()).orElse(List.of());
    List<TaskPayload> tasks = Optional.ofNullable(payload.getTasks()).orElse(List.of());
    ModelAssetPayload modelAsset = payload.getModelAsset();

    long now = System.currentTimeMillis();

    // ------------------ 追踪 Messages ------------------
    System.out.println("\n[钩子节点 1]: 开始处理 Messages 数组，共 " + messages.size() + " 条");
    for (MessagePayload messagePayload : messages) {
        String content = safeText(messagePayload.getText());
        String imagePreview = safeText(messagePayload.getImagePreview());

        System.out.println("  -> 提取消息内容 - Text: [" + content + "], ImagePreview: [" + imagePreview + "]");

        if (content.isBlank() && imagePreview.isBlank()) {
            System.out.println("  -> [拦截] 文本和图片均为空，已丢弃此消息。");
            continue;
        }

        try {
            ArchiveMessageEntity message = new ArchiveMessageEntity();
            message.setId(defaultIfBlank(messagePayload.getId(), idGenerator.newId("msg")));
            message.setArchive(archive);
            message.setRole(normalizeMessageRole(messagePayload.getRole()));
            message.setContent(content);
            message.setImagePreview(blankToNull(imagePreview));
            message.setCreatedAt(messagePayload.getCreatedAt() != null ? messagePayload.getCreatedAt() : now);

            archiveMessageRepository.save(message);
            archiveMessageRepository.flush(); // 强制立刻写入数据库，绝不延迟！
            System.out.println("  -> [成功] 消息已强制写入数据库！");
        } catch (Exception e) {
            System.err.println("  -> [致命错误] 保存消息到数据库失败！原因: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------ 追踪 Tasks ------------------
    System.out.println("\n[钩子节点 2]: 开始处理 Tasks");
    for (TaskPayload taskPayload : tasks) {
        ArchiveTaskEntity task = new ArchiveTaskEntity();
        task.setId(defaultIfBlank(taskPayload.getId(), idGenerator.newId("task")));
        task.setArchive(archive);
        task.setStatus(normalizeTaskStatus(taskPayload.getStatus()));
        task.setProgress(clamp(taskPayload.getProgress(), 0, 100));
        task.setPrompt(defaultIfBlank(taskPayload.getPrompt(), ""));
        task.setImageName(blankToNull(taskPayload.getImageName()));
        task.setImagePreview(normalizeTaskImagePreview(taskPayload.getImagePreview()));
        task.setCreatedAt(taskPayload.getCreatedAt() != null ? taskPayload.getCreatedAt() : now);
        task.setUpdatedAt(taskPayload.getUpdatedAt() != null ? taskPayload.getUpdatedAt() : task.getCreatedAt());
        archiveTaskRepository.save(task);
    }

    // ------------------ 追踪 Model Asset ------------------
    System.out.println("\n[钩子节点 3]: 开始处理 ModelAsset (模型资产)");
    System.out.println(">> 前端传来的 ModelAsset 对象是否为空: " + (modelAsset == null ? "是 (NULL)" : "否 (有数据)"));

    if (modelAsset != null) {
        String incomingImagePreview = modelAsset.getImagePreview();
        System.out.println(">> 前端传来的图片路径: [" + incomingImagePreview + "]");

        if (archive.getId() == null || archive.getId().isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "存档ID为空，无法保存模型资源");
        }

        try {
            ArchiveModelAssetEntity asset = archiveModelAssetRepository.findById(archive.getId())
                    .orElseGet(() -> {
                        System.out.println(">> 数据库查询: 没找到旧资产，准备新建...");
                        ArchiveModelAssetEntity newAsset = new ArchiveModelAssetEntity();
                        newAsset.setArchive(archive);
                        return newAsset;
                    });

            System.out.println(">> 数据库查询: 已准备好实体。当前数据库中的旧图片路径: [" + asset.getImagePreview() + "]");

            asset.setPrompt(defaultIfBlank(modelAsset.getPrompt(), ""));
            asset.setImagePreview(blankToNull(incomingImagePreview));
            asset.setCreatedAt(modelAsset.getCreatedAt() != null ? modelAsset.getCreatedAt() : now);
            asset.setUpdatedAt(modelAsset.getUpdatedAt() != null ? modelAsset.getUpdatedAt() : asset.getCreatedAt());

            System.out.println(">> 开始执行 save 动作...");
            archiveModelAssetRepository.save(asset);

            System.out.println(">> 开始执行 flush (强制写入) 动作...");
            archiveModelAssetRepository.flush(); // 强制立刻写入，揪出所有隐藏的 SQL 错误！

            System.out.println(">> [大功告成] ModelAsset 成功写入数据库！");
        } catch (Exception e) {
            System.err.println(">> [致命错误] 保存 ModelAsset 崩溃！错误信息如下:");
            e.printStackTrace();
        }
    } else {
        System.out.println(">> 前端传来的 ModelAsset 为 null，执行删除操作。");
        try {
            archiveModelAssetRepository.deleteById(archive.getId());
            archiveModelAssetRepository.flush();
        } catch(Exception e) {
            System.err.println(">> [错误] 删除 ModelAsset 失败: " + e.getMessage());
        }
    }
    System.out.println("============= [数据追踪钩子: 处理结束] =============\n");
}
    private String normalizeArchiveStatus(String status) {
        String normalized = safeText(status);
        return ARCHIVE_STATUS.contains(normalized) ? normalized : "idle";
    }

    private String normalizeTaskStatus(String status) {
        String normalized = safeText(status);
        return TASK_STATUS.contains(normalized) ? normalized : "queued";
    }

    private String normalizeMessageRole(String role) {
        String normalized = safeText(role);
        return MESSAGE_ROLE.contains(normalized) ? normalized : "user";
    }

    private int clamp(Integer value, int min, int max) {
        if (value == null) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    private String safeText(String value) {
        return Optional.ofNullable(value).orElse("").trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        String normalized = safeText(value);
        return normalized.isBlank() ? fallback : normalized;
    }

    private String blankToNull(String value) {
        String normalized = safeText(value);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeTaskImagePreview(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        return normalized;
    }

    private void deleteArchiveWithDependencies(String archiveId) {
        remixEventRepository.deleteByRemixerArchive_Id(archiveId);
        remixEventRepository.deleteByPublication_Archive_Id(archiveId);
        communityPublicationRepository.deleteByArchive_Id(archiveId);
        archiveRepository.clearSourceArchiveReferences(archiveId);
        archiveMessageRepository.deleteByArchive_Id(archiveId);
        archiveTaskRepository.deleteByArchive_Id(archiveId);
        archiveModelAssetRepository.deleteByArchiveId(archiveId);
        archiveRepository.deleteById(archiveId);
    }
}
