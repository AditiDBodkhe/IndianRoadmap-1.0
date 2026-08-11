package com.indianroadmap.roadmap.client;

public record DestinationSummary(
    String id,
    String slug,
    String name,
    double latitude,
    double longitude,
    int elevationMeters
) {
}
