package com.indianroadmap.audio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI audioServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IndianRoadmap — Audio Service")
                        .version("1.0.0")
                        .description("Multilingual TTS narration and audio metadata management for IndianRoadmap destinations and stories."));
    }
}
