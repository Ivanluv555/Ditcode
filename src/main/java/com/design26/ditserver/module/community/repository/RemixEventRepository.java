package com.design26.ditserver.module.community.repository;

import com.design26.ditserver.module.community.entity.RemixEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * RemixEventRepository - Remix 事件的数据访问接口。
 *
 * 业务角色：提供对 remix_events 表的常用操作（按 remixer archive 清理、按 publication archive 清理等），
 * 供 CommunityService 在删除/回滚存档时维护关联事件。
 */
public interface RemixEventRepository extends JpaRepository<RemixEventEntity, String> {
    @Transactional
    void deleteByRemixerArchive_Id(String archiveId);

    @Transactional
    void deleteByPublication_Archive_Id(String archiveId);
}
