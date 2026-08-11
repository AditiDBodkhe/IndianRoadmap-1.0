package com.indianroadmap.roadmap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DestinationClientConfig {

    private final String destinationBaseUrl;

    public DestinationClientConfig(@Value("${indianroadmap.services.destination.base-url}") String destinationBaseUrl) {
        this.destinationBaseUrl = destinationBaseUrl;
    }

    @Bean("destinationRestClient")
    public RestClient destinationRestClient() {
        return RestClient.builder()
            .baseUrl(destinationBaseUrl)
            .build();
    }
}
