package com.indianroadmap.roadmap.dto.response;

public record RouteSummaryResponse(
        double totalDistanceKm,
        int totalTravelTimeMinutes,
        int highestElevationMeters,
        int lowestElevationMeters,
        int elevationGainMeters,
        int nodeCount,
        int edgeCount
) {}
