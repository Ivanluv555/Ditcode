package com.design26.ditserver.core.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    public String newId(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
