package com.indianroadmap.recommendation.explanation;

import com.indianroadmap.recommendation.client.StorySummary;
import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.TravelStyle;
import com.indianroadmap.recommendation.scoring.ScoringResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates human-readable reasons for each recommendation.
 * Every reason corresponds to an actual scoring factor — no generic text.
 */
@Component
public class RecommendationExplanationGenerator {

    public List<String> generate(ScoringResult result, Mood requestedMood, String requestedRegion,
                                  List<StorySummary> stories) {
        var reasons = new ArrayList<String>();

        // Mood reasons
        if (!result.matchedMoods().isEmpty()) {
            String moodLabel = formatMood(requestedMood);
            if (result.moodScore() >= 30.0) {
                reasons.add("Perfectly matches your %s mood".formatted(moodLabel));
            } else if (result.moodScore() >= 15.0) {
                reasons.add("Good compatibility with your %s mood".formatted(moodLabel));
            }
        } else if (requestedMood != null && result.moodScore() > 0) {
            reasons.add("Partial alignment with your %s mood preference".formatted(formatMood(requestedMood)));
        }

        // Interest reasons
        for (Interest interest : result.matchedInterests()) {
            reasons.add("Strong %s alignment".formatted(formatInterest(interest)));
        }

        // Travel style reasons
        for (TravelStyle style : result.matchedTravelStyles()) {
            if (result.travelStyleScore() >= 15.0) {
                reasons.add("Ideal for %s".formatted(formatTravelStyle(style)));
            } else {
                reasons.add("Good fit for %s".formatted(formatTravelStyle(style)));
            }
        }

        // Region reasons
        if (requestedRegion != null && !requestedRegion.isBlank() && result.regionScore() >= 10.0) {
            reasons.add("Matches your preferred region (%s)".formatted(requestedRegion));
        } else if (result.regionScore() >= 7.0) {
            reasons.add("Located in a related region");
        }

        // Duration reasons
        if (result.durationScore() >= 10.0) {
            reasons.add("Ideal trip duration fits your plan");
        } else if (result.durationScore() >= 5.0) {
            reasons.add("Trip duration is close to your preference");
        }

        // Budget reasons
        if (result.budgetScore() >= 5.0) {
            reasons.add("Fits within your travel budget");
        }

        // Season reasons
        if (result.seasonScore() >= 5.0) {
            reasons.add("Best visited during your preferred season");
        }

        // Story-based enrichment
        if (!stories.isEmpty()) {
            long spiritualCount = stories.stream()
                    .filter(s -> "SPIRITUAL".equals(s.storyType())).count();
            long cultureCount = stories.stream()
                    .filter(s -> "CULTURE".equals(s.storyType()) || "HISTORY".equals(s.storyType())).count();
            long adventureCount = stories.stream()
                    .filter(s -> "ADVENTURE".equals(s.storyType())).count();

            if (spiritualCount > 0) reasons.add("Rich spiritual and cultural heritage documented");
            else if (cultureCount > 0) reasons.add("Detailed cultural and historical narratives available");
            else if (adventureCount > 0) reasons.add("Exciting adventure stories documented");
        }

        // Fallback
        if (reasons.isEmpty()) {
            reasons.add("Recommended based on your travel preferences");
        }

        return List.copyOf(reasons);
    }

    private String formatMood(Mood mood) {
        if (mood == null) return "travel";
        return switch (mood) {
            case ZEN -> "Zen";
            case ADVENTUROUS -> "adventurous";
            case SPIRITUAL -> "spiritual";
            case CURIOUS -> "curious";
            case ROMANTIC -> "romantic";
            case CULTURAL -> "cultural";
            case OFFBEAT -> "offbeat";
            case SOCIAL -> "social";
            case SOLITUDE -> "solitude";
            case FAMILY -> "family";
        };
    }

    private String formatInterest(Interest interest) {
        return switch (interest) {
            case NATURE -> "nature";
            case MOUNTAINS -> "mountain";
            case HISTORY -> "history";
            case CULTURE -> "culture";
            case FOOD -> "food & cuisine";
            case PHOTOGRAPHY -> "photography";
            case ASTRONOMY -> "astronomy";
            case SPIRITUALITY -> "spirituality";
            case ADVENTURE -> "adventure";
            case WILDLIFE -> "wildlife";
            case ARCHITECTURE -> "architecture";
            case LOCAL_LIFE -> "local life";
            case ROAD_TRIPS -> "road trip";
            case VILLAGES -> "village life";
        };
    }

    private String formatTravelStyle(TravelStyle style) {
        return switch (style) {
            case BACKPACKER -> "backpacking";
            case LUXURY -> "luxury travel";
            case SLOW_TRAVEL -> "slow travel";
            case ROAD_TRIP -> "road trips";
            case SOLO -> "solo travel";
            case COUPLE -> "couple travel";
            case FAMILY -> "family travel";
            case OFFBEAT -> "offbeat exploration";
            case ADVENTURE -> "adventure travel";
        };
    }
}
