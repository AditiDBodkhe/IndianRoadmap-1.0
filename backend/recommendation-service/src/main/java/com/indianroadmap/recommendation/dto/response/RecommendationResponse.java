package com.indianroadmap.recommendation.dto.response;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.MatchLevel;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.TravelStyle;

import java.util.List;

public record RecommendationResponse(
        DestinationSummaryDto destination,
        double score,
        MatchLevel matchLevel,
        List<String> reasons,
        List<Mood> matchedMoods,
        List<Interest> matchedInterests,
        List<TravelStyle> matchedTravelStyles
) {}
