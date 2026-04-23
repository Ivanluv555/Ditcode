package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ArchiveModelAssetRepository - 存档模型资源的数据访问接口。
 *
 * 业务角色：提供查询与删除存档模型产出的便捷方法，供 WorkspaceService 在保存与删除时使用。
 */
public interface ArchiveModelAssetRepository extends JpaRepository<ArchiveModelAssetEntity, String> {
    List<ArchiveModelAssetEntity> findByArchiveIdIn(List<String> archiveIds);

    @Transactional
    void deleteByArchiveId(String archiveId);
}
