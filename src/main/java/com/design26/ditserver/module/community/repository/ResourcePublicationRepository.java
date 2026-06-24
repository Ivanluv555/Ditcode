package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.ResourcePublicationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ResourcePublicationRepository - 资源库条目的数据访问接口。
 *
 * 提供对 resource_publications 表的基础查询方法（如获取激活的资源列表、按 archive 查询及删除）。
 */
public interface ResourcePublicationRepository extends JpaRepository<ResourcePublicationEntity, String> {
    List<ResourcePublicationEntity> findByIsActiveTrueOrderByUpdatedAtDesc();

    Optional<ResourcePublicationEntity> findByArchive_Id(String archiveId);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
