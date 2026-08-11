package com.indianroadmap.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, GatewayProperties props) {
        var services = props.services();

        return builder.routes()
                .route("user-auth", r -> r
                        .path("/api/v1/auth/**")
                        .uri(services.user()))
                .route("user-profile", r -> r
                        .path("/api/v1/users/**")
                        .uri(services.user()))
                .route("destinations", r -> r
                        .path("/api/v1/destinations/**")
                        .uri(services.destination()))
                .route("roadmaps", r -> r
                        .path("/api/v1/roadmaps/**")
                        .uri(services.roadmap()))
                .route("stories", r -> r
                        .path("/api/v1/stories/**")
                        .uri(services.story()))
                .route("audio", r -> r
                        .path("/api/v1/audio/**")
                        .uri(services.audio()))
                .route("recommendations", r -> r
                        .path("/api/v1/recommendations/**")
                        .uri(services.recommendation()))
                .route("recommendation-profiles", r -> r
                        .path("/api/v1/recommendation-profiles/**")
                        .uri(services.recommendation()))
                .route("ai-service", r -> r
                        .path("/api/ai/**")
                        .uri(services.ai()))
                .build();
    }
}
