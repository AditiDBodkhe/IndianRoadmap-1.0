package com.indianroadmap.recommendation.dto.response;

public record DestinationSummaryDto(
        String id,
        String slug,
        String name,
        String state,
        String region,
        java.util.List<String> categories,
        java.util.List<String> moods
) {}
