package com.codearena.code_arena_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register JavaTimeModule so that LocalDateTime is serialized correctly
        // (e.g. notification and message timestamps)
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
