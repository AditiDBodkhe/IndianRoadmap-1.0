package com.indianroadmap.roadmap.dto.request;

import com.indianroadmap.roadmap.document.RoadType;
import com.indianroadmap.roadmap.document.RouteDifficulty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddRoadmapEdgeRequest(
        @NotBlank(message = "From node ID must not be blank") String fromNodeId,
        @NotBlank(message = "To node ID must not be blank") String toNodeId,
        @DecimalMin(value = "0.001", message = "Distance must be greater than zero") double distanceKm,
        @Positive(message = "Travel time must be positive") int estimatedTravelTimeMinutes,
        @NotNull(message = "Road type must not be null") RoadType roadType,
        @NotNull(message = "Difficulty must not be null") RouteDifficulty difficulty
) {}
