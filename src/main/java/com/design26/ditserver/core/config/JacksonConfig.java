package com.design26.ditserver.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Jackson配置类，提供一个ObjectMapper的Bean，供全局使用
// 把json变成对象，或者把对象变成json，都要用到这个ObjectMapper
// 稍微封装一下
@Configuration
public class JacksonConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
