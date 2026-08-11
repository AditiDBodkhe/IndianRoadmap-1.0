package com.indianroadmap.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.security.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds
) {
}
