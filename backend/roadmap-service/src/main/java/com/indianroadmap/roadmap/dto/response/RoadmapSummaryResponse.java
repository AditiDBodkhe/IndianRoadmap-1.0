package com.indianroadmap.roadmap.dto.response;

import com.indianroadmap.roadmap.document.RoadmapStatus;

import java.time.Instant;

public record RoadmapSummaryResponse(
        String id,
        String slug,
        String name,
        String description,
        RoadmapStatus status,
        int nodeCount,
        int edgeCount,
        double totalDistanceKm,
        Instant createdAt,
        Instant updatedAt
) {
}
