package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveMessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArchiveMessageRepository - 存档消息的数据访问接口。
 *
 * 业务角色：为 Workspace 提供按存档批量查询消息和删除消息的方法，支持 WorkspaceService 的聚合场景。
 */
public interface ArchiveMessageRepository extends JpaRepository<ArchiveMessageEntity, String> {
    List<ArchiveMessageEntity> findByArchive_IdInOrderByCreatedAtAsc(List<String> archiveIds);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
