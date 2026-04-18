package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveTaskEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ArchiveTaskRepository extends JpaRepository<ArchiveTaskEntity, String> {
    List<ArchiveTaskEntity> findByArchive_IdInOrderByCreatedAtAsc(List<String> archiveIds);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
