package com.indianroadmap.audio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audio.tts")
public record AudioTtsProperties(String provider) {}
