package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArchiveRepository - 用户工作区（Archive）数据访问接口。
 *
 * 业务角色：定义对 archives 表的常用查询与修改方法（按所有者获取、按ID检查、清理来源引用等），
 * 供 WorkspaceService 在持久化与删除操作中使用。
 */
public interface ArchiveRepository extends JpaRepository<ArchiveEntity, String> {
    List<ArchiveEntity> findByOwner_IdAndDeletedAtIsNullOrderByUpdatedAtDesc(String ownerId);

    Optional<ArchiveEntity> findByIdAndOwner_IdAndDeletedAtIsNull(String id, String ownerId);

    Optional<ArchiveEntity> findTopByOwner_IdAndSourceCommunityIdAndDeletedAtIsNullOrderByUpdatedAtDesc(
        String ownerId,
        String sourceCommunityId
    );

    @Modifying
    @Transactional
    @Query("update ArchiveEntity a set a.sourceArchive = null where a.sourceArchive.id = :sourceArchiveId")
    int clearSourceArchiveReferences(@Param("sourceArchiveId") String sourceArchiveId);
}
