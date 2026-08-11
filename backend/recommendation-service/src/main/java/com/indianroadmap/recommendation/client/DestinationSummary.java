package com.indianroadmap.recommendation.client;

import java.util.List;

/**
 * Recommendation-relevant subset of destination data.
 * The name field is the resolved display name (defaultName or slug).
 */
public record DestinationSummary(
        String id,
        String slug,
        String name,
        String state,
        String region,
        List<String> categories,
        List<String> moods
) {}
