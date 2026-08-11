package com.indianroadmap.story.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DestinationClientConfig {

    private final DestinationServiceProperties properties;

    public DestinationClientConfig(DestinationServiceProperties properties) {
        this.properties = properties;
    }

    @Bean("storyDestinationRestClient")
    public RestClient storyDestinationRestClient() {
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .build();
    }
}
