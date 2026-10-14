package com.vibesync.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil — handles JWT token creation and validation.
 *
 * A JWT token looks like: xxxxx.yyyyy.zzzzz
 *   - xxxxx = header (algorithm type)
 *   - yyyyy = payload (email, expiry date)
 *   - zzzzz = signature (proves the token was made by us)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs; // default: 86400000 = 24 hours in milliseconds

    // Build the secret key from the string in application.properties
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate a new JWT token for a given user email
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract the email (subject) from a token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Check if the token has expired
    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // any parse error = invalid token
        }
    }

    // Parse the token and return its claims (the payload data)
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
