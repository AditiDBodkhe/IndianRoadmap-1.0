package com.indianroadmap.user.service;

import com.indianroadmap.user.config.JwtProperties;
import com.indianroadmap.user.document.UserRole;
import com.indianroadmap.user.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtService(JwtProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String userId, UserRole role) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(props.accessTokenExpirationSeconds());
        return Jwts.builder()
                .subject(userId)
                .claim("role", role.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.toInstant().isAfter(Instant.now(clock));
        } catch (Exception ignored) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public UserRole extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return UserRole.valueOf(role);
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            return expiration == null || !expiration.toInstant().isAfter(Instant.now(clock));
        } catch (Exception ex) {
            return true;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return props.accessTokenExpirationSeconds();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .clock(() -> Date.from(Instant.now(clock)))
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw new InvalidTokenException("JWT is invalid or expired");
        }
    }
}
