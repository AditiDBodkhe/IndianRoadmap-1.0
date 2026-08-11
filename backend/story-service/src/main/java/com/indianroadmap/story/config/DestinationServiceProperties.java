package com.indianroadmap.story.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.services.destination")
public record DestinationServiceProperties(String baseUrl) {
}
