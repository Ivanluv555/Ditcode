package com.design26.ditserver.module.workspace.repository;

import com.design26.ditserver.module.workspace.entity.ArchiveEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<ArchiveEntity, String> {
    List<ArchiveEntity> findByOwner_IdAndDeletedAtIsNullOrderByUpdatedAtDesc(String ownerId);

    Optional<ArchiveEntity> findByIdAndOwner_IdAndDeletedAtIsNull(String id, String ownerId);

    Optional<ArchiveEntity> findTopByOwner_IdAndSourceCommunityIdAndDeletedAtIsNullOrderByUpdatedAtDesc(
        String ownerId,
        String sourceCommunityId
    );
}
