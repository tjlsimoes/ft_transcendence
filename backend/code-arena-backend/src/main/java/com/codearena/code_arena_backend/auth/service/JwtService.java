package com.codearena.code_arena_backend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Centralises all JWT operations.
 *
 * Token anatomy:
 *   Header  – algorithm (HS256) + type (JWT)
 *   Payload – claims: subject (username), issued-at, expiration
 *   Signature – HMAC-SHA256 of header + payload using the secret key
 *
 * The secret MUST be at least 256 bits when base64-decoded (32 bytes).
 * It is read from the environment variable JWT_SECRET via application.properties.
 */
@Service
public class JwtService {

    /** Injected from jwt.secret property → ${JWT_SECRET} env var. */
    @Value("${jwt.secret}")
    private String secretKey;

    /** Token lifetime in milliseconds, defaults to 24 h (86 400 000 ms). */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Generates a JWT for the given UserDetails with no extra claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT with additional custom claims (e.g. roles, userId).
     *
     * @param extraClaims any additional key-value pairs to embed in the token
     * @param userDetails Spring Security user (subject = username)
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                           // custom claims first
                .subject(userDetails.getUsername())            // 'sub' standard claim
                .issuedAt(new Date(System.currentTimeMillis())) // 'iat'
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 'exp'
                .signWith(getSigningKey())                     // signs with HS256
                .compact();
    }

    /**
     * Returns true if the token belongs to the given user and is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Extracts the 'sub' claim (username) from the token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    /** Generic claim extractor – accepts any Claims → T function. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Parses and verifies the token signature, returning all claims.
     * Throws JwtException (unchecked) if the token is invalid or tampered.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the base64 secret and wraps it in an HMAC-SHA key.
     * Keys.hmacShaKeyFor enforces the minimum key length required by HS256.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
