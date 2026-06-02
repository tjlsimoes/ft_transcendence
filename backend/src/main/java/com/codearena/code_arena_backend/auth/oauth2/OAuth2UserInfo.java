package com.codearena.code_arena_backend.auth.oauth2;

public record OAuth2UserInfo(
    String id,
    String username,
    String email,
    String avatarUrl
) {}
