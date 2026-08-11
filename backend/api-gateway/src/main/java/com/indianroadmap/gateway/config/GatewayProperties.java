package com.indianroadmap.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap")
public record GatewayProperties(
        Services services,
        Gateway gateway
) {
    public record Services(
            String destination,
            String roadmap,
            String story,
            String audio,
            String recommendation,
            String user,
            String ai
    ) {
    }

    public record Gateway(Timeout timeout) {
        public record Timeout(int connectMs, int responseMs) {
        }
    }
}
