package com.indianroadmap.audio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StoryClientConfig {

    private final StoryServiceProperties properties;

    public StoryClientConfig(StoryServiceProperties properties) {
        this.properties = properties;
    }

    @Bean("audioStoryRestClient")
    public RestClient audioStoryRestClient() {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}
