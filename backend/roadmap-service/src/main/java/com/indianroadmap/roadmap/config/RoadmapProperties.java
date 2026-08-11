package com.indianroadmap.roadmap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indianroadmap.roadmap")
public record RoadmapProperties(int maxNodesPerRoadmap, int maxEdgesPerRoadmap) {
}
