package com.indianroadmap.roadmap.dto.response;

import com.indianroadmap.roadmap.document.RoadType;
import com.indianroadmap.roadmap.document.RouteDifficulty;

public record RoadmapEdgeResponse(
        String edgeId,
        String fromNodeId,
        String toNodeId,
        double distanceKm,
        int estimatedTravelTimeMinutes,
        RoadType roadType,
        RouteDifficulty difficulty
) {}
