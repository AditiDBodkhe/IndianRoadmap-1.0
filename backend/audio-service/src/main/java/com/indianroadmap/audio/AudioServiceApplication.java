package com.indianroadmap.audio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AudioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioServiceApplication.class, args);
    }
}
