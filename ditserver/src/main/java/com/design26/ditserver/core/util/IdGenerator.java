package com.design26.ditserver.core.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * IdGenerator - 简单唯一ID生成器。
 *
 * 业务角色：为系统中需要可读前缀的资源（如 user, task, archive 等）生成唯一ID，
 * 目前用于测试与示例场景，生成规则可替换为更可靠的雪花ID/UUID方案。
 */
@Component
public class IdGenerator {

    public String newId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
