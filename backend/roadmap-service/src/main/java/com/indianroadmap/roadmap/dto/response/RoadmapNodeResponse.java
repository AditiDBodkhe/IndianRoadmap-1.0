package com.indianroadmap.roadmap.dto.response;

import com.indianroadmap.roadmap.document.RoadmapNodeRole;

public record RoadmapNodeResponse(
        String nodeId,
        String destinationId,
        int sequence,
        String label,
        RoadmapNodeRole role,
        int elevationMeters
) {}
