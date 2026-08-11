package com.indianroadmap.roadmap.dto.response;

import com.indianroadmap.roadmap.document.RoadmapStatus;

import java.time.Instant;
import java.util.List;

public record RoadmapResponse(
        String id,
        String slug,
        String name,
        String description,
        RoadmapStatus status,
        List<RoadmapNodeResponse> nodes,
        List<RoadmapEdgeResponse> edges,
        RouteSummaryResponse routeSummary,
        Instant createdAt,
        Instant updatedAt
) {}
