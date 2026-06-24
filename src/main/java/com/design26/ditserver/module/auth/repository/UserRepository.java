package com.design26.ditserver.module.auth.repository;

import com.design26.ditserver.module.auth.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserRepository - 用户实体的数据访问接口。
 *
 * 业务角色：定义对 users 表的常用查询方法（按用户名/邮箱查找、存在性检查等），供 AuthService 使用。
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findFirstByOrderByCreatedAtAsc();

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
