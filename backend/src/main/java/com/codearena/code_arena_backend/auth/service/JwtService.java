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
import java.util.UUID;
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

    /**
     * Token lifetime in milliseconds — injected for calculating 'expiresIn'
     * seconds.
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /** Refresh token lifetime in milliseconds. */
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Generates a standard access JWT for the given UserDetails.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, jwtExpiration, "access");
    }

    /**
     * Generates a long-lived refresh token for the given UserDetails.
     * Includes a unique JTI (JWT ID) for revocation/rotation tracking.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", UUID.randomUUID().toString());
        return generateToken(claims, userDetails, refreshExpiration, "refresh");
    }

    /**
     * Internal helper to generate a JWT with specific claims and expiration.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration,
            String type) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("type", type);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Returns true if the token belongs to the given user and is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "access");
    }

    /**
     * Validates a token for a specific user and expected type.
     */
    public boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        try {
            final String username = extractUsername(token);
            final String type = extractClaim(token, claims -> claims.get("type", String.class));
            return (username.equals(userDetails.getUsername()))
                    && type.equals(expectedType)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /** Extracts the 'sub' claim (username) from the token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the "type" claim (access or refresh).
     */
    public String extractType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extracts the "jti" (JWT ID) claim (primary for refresh tokens).
     */
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    /** Extracts the 'exp' claim from the token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
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
