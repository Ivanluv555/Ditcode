package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.CommunityPublicationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPublicationRepository extends JpaRepository<CommunityPublicationEntity, String> {
    List<CommunityPublicationEntity> findByIsActiveTrueOrderByUpdatedAtDesc();

    Optional<CommunityPublicationEntity> findByArchive_Id(String archiveId);
}
