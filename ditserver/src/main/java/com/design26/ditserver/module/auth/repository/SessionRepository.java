package com.design26.ditserver.module.auth.repository;

import com.design26.ditserver.module.auth.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    Optional<SessionEntity> findByToken(String token);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByUser_Id(String userId);
}
