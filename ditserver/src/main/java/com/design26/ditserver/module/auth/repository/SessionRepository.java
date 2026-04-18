package com.design26.ditserver.module.auth.repository;

import com.design26.ditserver.module.auth.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    // 根据Token查询会话信息和关联的用户信息
    // 这个查询比较特殊，JPA默认的查询方法不太适合，就手写一个
    Optional<SessionEntity> findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByUser_Id(String userId);
}
