package com.indianroadmap.recommendation.scoring;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.Season;
import com.indianroadmap.recommendation.document.TravelStyle;

import java.util.List;
import java.util.Set;

/**
 * Immutable scoring context carrying both the user request and the candidate destination profile.
 */
public record ScoringContext(
        // User preferences
        Mood requestedMood,
        List<Interest> requestedInterests,
        TravelStyle requestedTravelStyle,
        Integer requestedDurationDays,
        Integer requestedMaxBudget,
        String requestedRegion,
        Season requestedSeason,

        // Destination profile
        String destinationId,
        List<Mood> profileMoods,
        List<Interest> profileInterests,
        List<TravelStyle> profileTravelStyles,
        List<String> profileRegions,
        int profileDurationMin,
        int profileDurationMax,
        int profileBudgetMin,
        int profileBudgetMax,
        List<Season> profileSeasonTags,

        // Destination-service data (from live API)
        List<String> destinationMoods,
        List<String> destinationCategories
) {}
