package com.indianroadmap.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.security.jwt")
public record JwtProperties(String secret) {
}
