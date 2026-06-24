package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.CommunityPublicationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * CommunityPublicationRepository - 社区发布记录的数据访问接口。
 *
 * 业务角色：提供对 community_publications 表的查询与变更方法（如获取激活的发布列表、按 archive 查询等），
 * 供 CommunityService 使用。
 */
public interface CommunityPublicationRepository extends JpaRepository<CommunityPublicationEntity, String> {
    List<CommunityPublicationEntity> findByIsActiveTrueOrderByUpdatedAtDesc();

    Optional<CommunityPublicationEntity> findByArchive_Id(String archiveId);

    @Transactional
    void deleteByArchive_Id(String archiveId);
}
