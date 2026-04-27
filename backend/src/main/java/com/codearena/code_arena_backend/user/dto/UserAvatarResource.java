package com.codearena.code_arena_backend.user.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record UserAvatarResource(
        Resource resource,
        MediaType mediaType
) {
}
