package com.indianroadmap.recommendation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ServiceClientConfig {

    @Bean("recDestinationRestClient")
    public RestClient destinationRestClient(DestinationServiceProperties props) {
        return RestClient.builder().baseUrl(props.baseUrl()).build();
    }

    @Bean("recStoryRestClient")
    public RestClient storyRestClient(StoryServiceProperties props) {
        return RestClient.builder().baseUrl(props.baseUrl()).build();
    }
}
