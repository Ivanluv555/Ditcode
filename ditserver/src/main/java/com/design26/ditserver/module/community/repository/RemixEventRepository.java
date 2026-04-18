package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.RemixEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RemixEventRepository extends JpaRepository<RemixEventEntity, String> {
    @Transactional
    void deleteByRemixerArchive_Id(String archiveId);

    @Transactional
    void deleteByPublication_Archive_Id(String archiveId);
}
