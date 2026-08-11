package com.indianroadmap.recommendation.dto.response;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.Season;
import com.indianroadmap.recommendation.document.TravelStyle;

import java.time.Instant;
import java.util.List;

public record RecommendationProfileResponse(
        String id,
        String destinationId,
        List<Mood> moods,
        List<Interest> interests,
        List<TravelStyle> travelStyles,
        List<String> regions,
        int idealDurationMin,
        int idealDurationMax,
        int budgetMin,
        int budgetMax,
        List<Season> seasonTags,
        String difficulty,
        double weight,
        Instant createdAt,
        Instant updatedAt
) {}
