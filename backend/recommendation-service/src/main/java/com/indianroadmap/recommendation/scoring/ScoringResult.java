package com.indianroadmap.recommendation.scoring;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.TravelStyle;

import java.util.List;

/**
 * Immutable result of scoring a candidate destination.
 */
public record ScoringResult(
        String destinationId,
        double totalScore,
        double moodScore,
        double interestScore,
        double travelStyleScore,
        double regionScore,
        double durationScore,
        double budgetScore,
        double seasonScore,
        List<Mood> matchedMoods,
        List<Interest> matchedInterests,
        List<TravelStyle> matchedTravelStyles
) {
    public static final double MAX_SCORE = 100.0;
}
