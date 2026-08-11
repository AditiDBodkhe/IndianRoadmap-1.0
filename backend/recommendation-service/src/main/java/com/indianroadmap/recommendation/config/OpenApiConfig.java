package com.indianroadmap.recommendation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recommendationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IndianRoadmap — Recommendation Service")
                        .version("1.0.0")
                        .description("Mood-based, interest-based, and travel-style destination recommendations for IndianRoadmap."));
    }
}
