package com.indianroadmap.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.services.destination")
public record DestinationServiceProperties(String baseUrl) {}
