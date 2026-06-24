package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.FeaturedPublicationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * FeaturedPublicationRepository - 精选条目的数据访问接口。
 *
 * 提供对 featured_publications 表的基础查询方法（如获取激活的精选列表、按 archive 查询及删除）。
 */
public interface FeaturedPublicationRepository extends JpaRepository<FeaturedPublicationEntity, String> {
    List<FeaturedPublicationEntity> findByIsActiveTrueOrderByUpdatedAtDesc();

    Optional<FeaturedPublicationEntity> findByArchive_Id(String archiveId);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
