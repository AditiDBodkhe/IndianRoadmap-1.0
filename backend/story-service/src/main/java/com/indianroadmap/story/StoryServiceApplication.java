package com.indianroadmap.story;

import com.indianroadmap.story.config.DestinationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DestinationServiceProperties.class)
public class StoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoryServiceApplication.class, args);
    }
}
