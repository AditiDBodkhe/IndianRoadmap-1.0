package com.indianroadmap.recommendation.dto.request;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.Season;
import com.indianroadmap.recommendation.document.TravelStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RecommendationProfileRequest(

        @NotBlank(message = "destinationId must not be blank")
        String destinationId,

        @NotEmpty(message = "moods must not be empty")
        List<@NotNull Mood> moods,

        List<Interest> interests,

        List<TravelStyle> travelStyles,

        List<String> regions,

        @Positive(message = "idealDurationMin must be positive")
        int idealDurationMin,

        @Positive(message = "idealDurationMax must be positive")
        int idealDurationMax,

        int budgetMin,

        int budgetMax,

        List<Season> seasonTags,

        String difficulty
) {}
