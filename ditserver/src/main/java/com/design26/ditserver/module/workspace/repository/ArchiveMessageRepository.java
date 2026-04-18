package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveMessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ArchiveMessageRepository extends JpaRepository<ArchiveMessageEntity, String> {
    List<ArchiveMessageEntity> findByArchive_IdInOrderByCreatedAtAsc(List<String> archiveIds);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
