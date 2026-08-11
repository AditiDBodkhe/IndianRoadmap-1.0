package com.indianroadmap.destination.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI destinationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IndianRoadmap — Destination Service API")
                        .description("REST API for managing Indian travel destinations: coordinates, elevation, cultural metadata, geospatial search")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IndianRoadmap")
                                .url("https://indianroadmap.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
