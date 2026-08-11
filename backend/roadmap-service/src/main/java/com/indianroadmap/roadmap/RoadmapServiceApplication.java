package com.indianroadmap.roadmap;

import com.indianroadmap.roadmap.config.RoadmapProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RoadmapProperties.class)
public class RoadmapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadmapServiceApplication.class, args);
    }
}
