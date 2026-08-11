package com.indianroadmap.user.service;

import com.indianroadmap.user.config.JwtProperties;
import com.indianroadmap.user.document.RefreshTokenDocument;
import com.indianroadmap.user.exception.RefreshTokenExpiredException;
import com.indianroadmap.user.exception.RefreshTokenInvalidException;
import com.indianroadmap.user.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final Clock clock;
    private final JwtProperties props;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository repository,
                               Clock clock,
                               JwtProperties props,
                               JwtService jwtService) {
        this.repository = repository;
        this.clock = clock;
        this.props = props;
        this.jwtService = jwtService;
    }

    public String createRefreshToken(String userId) {
        Instant now = Instant.now(clock);
        String rawToken = jwtService.generateRefreshToken();

        RefreshTokenDocument document = new RefreshTokenDocument();
        document.setUserId(userId);
        document.setTokenHash(hashToken(rawToken));
        document.setCreatedAt(now);
        document.setExpiresAt(now.plusSeconds(props.refreshTokenExpirationSeconds()));
        repository.save(document);
        return rawToken;
    }

    public String validateAndRotate(String rawToken) {
        return rotateToken(requireValidToken(rawToken));
    }

    public RefreshTokenDocument requireValidToken(String rawToken) {
        RefreshTokenDocument document = repository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(RefreshTokenInvalidException::new);

        if (document.getRevokedAt() != null) {
            throw new RefreshTokenInvalidException();
        }
        if (!document.getExpiresAt().isAfter(Instant.now(clock))) {
            throw new RefreshTokenExpiredException();
        }
        return document;
    }

    public String rotateToken(RefreshTokenDocument document) {
        document.setRevokedAt(Instant.now(clock));
        repository.save(document);
        return createRefreshToken(document.getUserId());
    }

    public void revokeToken(String rawToken) {
        repository.findByTokenHash(hashToken(rawToken)).ifPresent(document -> {
            if (document.getRevokedAt() == null) {
                document.setRevokedAt(Instant.now(clock));
                repository.save(document);
            }
        });
    }

    public String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
