package com.indianroadmap.gateway;

import com.indianroadmap.gateway.config.CorsProperties;
import com.indianroadmap.gateway.config.GatewayProperties;
import com.indianroadmap.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GatewayProperties.class, JwtProperties.class, CorsProperties.class})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
