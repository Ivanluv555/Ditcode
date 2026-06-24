package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveTaskEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArchiveTaskRepository - 存档任务的数据访问接口。
 *
 * 业务角色：提供按存档批量获取任务和删除任务的方法，供 WorkspaceService 管理任务生命周期使用。
 */
public interface ArchiveTaskRepository extends JpaRepository<ArchiveTaskEntity, String> {
    List<ArchiveTaskEntity> findByArchive_IdInOrderByCreatedAtAsc(List<String> archiveIds);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
