package com.indianroadmap.recommendation.dto.request;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.Season;
import com.indianroadmap.recommendation.document.TravelStyle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RecommendationRequest(

        @NotNull(message = "mood must not be null")
        Mood mood,

        List<Interest> interests,

        TravelStyle travelStyle,

        @Positive(message = "durationDays must be positive")
        Integer durationDays,

        @Positive(message = "maxBudget must be positive")
        Integer maxBudget,

        String preferredRegion,

        Season season,

        @Min(value = 1, message = "limit must be at least 1")
        @Max(value = 50, message = "limit must not exceed 50")
        Integer limit
) {
    public int effectiveLimit() {
        return limit != null ? limit : 10;
    }
}
