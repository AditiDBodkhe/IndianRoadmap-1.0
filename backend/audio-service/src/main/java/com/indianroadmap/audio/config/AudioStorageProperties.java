package com.indianroadmap.audio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audio.storage")
public record AudioStorageProperties(String path) {}
