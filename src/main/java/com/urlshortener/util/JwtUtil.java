package com.urlshortener.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT UTILITY — Stateless Authentication
 *
 * HOW JWT WORKS (explain in interviews):
 * Token = Base64(Header) + "." + Base64(Payload) + "." + HMAC-SHA256(Header+Payload, secret)
 *
 * Header:  { "alg": "HS256", "typ": "JWT" }
 * Payload: { "sub": "user@email.com", "iat": 1234567890, "exp": 1234654290 }
 * Signature: HMAC-SHA256(header + "." + payload, secretKey)
 *
 * WHY STATELESS?
 * Server stores NO session. Any instance can validate any token.
 * This is what makes horizontal scaling (multiple servers) easy.
 * Just check the signature — if valid and not expired, user is authenticated.
 *
 * SECURITY:
 * - Secret key must be 256+ bits (32+ characters)
 * - Tokens expire after 24 hours (configurable)
 * - Signature prevents tampering — changing payload breaks signature
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry-ms}")
    private long expiryMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Create a new token for authenticated user */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Extract email from token payload */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /** Returns true only if token is properly signed AND not expired */
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Invalid signature, expired, or malformed
        }
    }
}