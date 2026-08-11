package com.indianroadmap.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.services.story")
public record StoryServiceProperties(String baseUrl) {}
