package com.indianroadmap.recommendation.client;

import java.util.List;

/**
 * Story context used to enrich recommendation explanations.
 */
public record StorySummary(
        String id,
        String storyType,
        String title,
        String status
) {}
