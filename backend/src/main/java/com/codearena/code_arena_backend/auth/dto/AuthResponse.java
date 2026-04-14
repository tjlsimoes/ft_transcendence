package com.codearena.code_arena_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response body returned after a successful login or registration.
 *
 * The client must include the token in every subsequent request:
 *   Authorization: Bearer <token>
 *
 * tokenType is always "Bearer" – it tells the client how to use the token.
 * expiresIn is the lifetime in seconds so the client knows when to refresh.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn; // seconds
}
