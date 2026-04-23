package com.design26.ditserver.module.auth.repository;

import com.design26.ditserver.module.auth.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * SessionRepository - 会话实体的数据访问接口。
 *
 * 业务角色：定义查询和变更 sessions 表的常用操作，提供按 token 查询并同时加载关联用户的便捷方法，
 * 供认证相关服务使用。
 */
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    // 根据Token查询会话信息和关联的用户信息
    // 这个查询比较特殊，JPA默认的查询方法不太适合，就手写一个
    @EntityGraph(attributePaths = "user")
    Optional<SessionEntity> findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByUser_Id(String userId);
}
