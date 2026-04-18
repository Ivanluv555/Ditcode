package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveModelAssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ArchiveModelAssetRepository extends JpaRepository<ArchiveModelAssetEntity, String> {
    List<ArchiveModelAssetEntity> findByArchiveIdIn(List<String> archiveIds);

    @Transactional
    void deleteByArchiveId(String archiveId);
}
