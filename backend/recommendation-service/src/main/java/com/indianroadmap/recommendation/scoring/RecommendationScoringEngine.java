package com.indianroadmap.recommendation.scoring;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.Season;
import com.indianroadmap.recommendation.document.TravelStyle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic rule-based scoring engine.
 *
 * Score breakdown (max 100):
 *   Mood match         30
 *   Interest match     25 (5 per interest, max 5 interests)
 *   Travel style       15
 *   Region match       10
 *   Duration match     10
 *   Budget match        5
 *   Season match        5
 *                    ----
 *   Total             100
 */
@Component
public class RecommendationScoringEngine {

    // Mood → compatible destination-service mood values (lowercase for flexible matching)
    private static final Map<Mood, Set<String>> MOOD_TO_DESTINATION_MOODS = Map.of(
            Mood.ZEN,         Set.of("ZEN", "SOLITUDE"),
            Mood.ADVENTUROUS, Set.of("ADVENTURE", "WILD"),
            Mood.SPIRITUAL,   Set.of("SPIRITUAL", "ZEN"),
            Mood.CURIOUS,     Set.of("CURIOUS", "HERITAGE", "WILD"),
            Mood.ROMANTIC,    Set.of("ZEN", "SOLITUDE", "WILD"),
            Mood.CULTURAL,    Set.of("HERITAGE", "SPIRITUAL", "CURIOUS"),
            Mood.OFFBEAT,     Set.of("WILD", "ADVENTURE", "SOLITUDE"),
            Mood.SOCIAL,      Set.of("HERITAGE", "CURIOUS"),
            Mood.SOLITUDE,    Set.of("SOLITUDE", "ZEN", "WILD"),
            Mood.FAMILY,      Set.of("HERITAGE", "SPIRITUAL", "CURIOUS", "ADVENTURE")
    );

    // Partial mood compatibility (secondary match)
    private static final Map<Mood, Set<String>> MOOD_PARTIAL_COMPAT = Map.of(
            Mood.ZEN,         Set.of("SPIRITUAL", "CURIOUS"),
            Mood.ADVENTUROUS, Set.of("CURIOUS", "ZEN"),
            Mood.SPIRITUAL,   Set.of("HERITAGE", "SOLITUDE"),
            Mood.CURIOUS,     Set.of("ADVENTURE", "SPIRITUAL"),
            Mood.ROMANTIC,    Set.of("SPIRITUAL", "HERITAGE"),
            Mood.CULTURAL,    Set.of("ADVENTURE", "WILD"),
            Mood.OFFBEAT,     Set.of("CURIOUS", "HERITAGE"),
            Mood.SOCIAL,      Set.of("ADVENTURE", "SPIRITUAL"),
            Mood.SOLITUDE,    Set.of("ADVENTURE", "SPIRITUAL"),
            Mood.FAMILY,      Set.of("WILD", "PATRIOTIC")
    );

    // Interest → destination categories and moods that match it
    private static final Map<Interest, Set<String>> INTEREST_CATEGORY_MAP;
    static {
        var entries = new java.util.HashMap<Interest, Set<String>>();
        entries.put(Interest.NATURE,        Set.of("VILLAGE", "MOUNTAIN", "PASS", "ADVENTURE"));
        entries.put(Interest.MOUNTAINS,     Set.of("MOUNTAIN", "PASS", "ADVENTURE"));
        entries.put(Interest.HISTORY,       Set.of("HERITAGE", "HISTORICAL", "BORDER"));
        entries.put(Interest.CULTURE,       Set.of("HERITAGE", "MONASTERY", "TEMPLE", "SPIRITUAL"));
        entries.put(Interest.SPIRITUALITY,  Set.of("SPIRITUAL", "TEMPLE", "MONASTERY"));
        entries.put(Interest.ADVENTURE,     Set.of("ADVENTURE", "MOUNTAIN", "PASS"));
        entries.put(Interest.ASTRONOMY,     Set.of("SCIENTIFIC"));
        entries.put(Interest.WILDLIFE,      Set.of("ADVENTURE", "MOUNTAIN"));
        entries.put(Interest.ARCHITECTURE,  Set.of("HERITAGE", "HISTORICAL", "MONASTERY", "TEMPLE"));
        entries.put(Interest.LOCAL_LIFE,    Set.of("VILLAGE", "BORDER"));
        entries.put(Interest.VILLAGES,      Set.of("VILLAGE"));
        entries.put(Interest.ROAD_TRIPS,    Set.of("PASS", "MOUNTAIN", "ADVENTURE"));
        entries.put(Interest.PHOTOGRAPHY,   Set.of("VILLAGE", "MOUNTAIN", "HERITAGE", "ADVENTURE"));
        entries.put(Interest.FOOD,          Set.of("CITY", "VILLAGE", "BORDER"));
        INTEREST_CATEGORY_MAP = Map.copyOf(entries);
    }

    // TravelStyle compatibility
    private static final Map<TravelStyle, Set<TravelStyle>> TRAVEL_STYLE_COMPAT = Map.of(
            TravelStyle.BACKPACKER,  Set.of(TravelStyle.SOLO, TravelStyle.ADVENTURE, TravelStyle.OFFBEAT),
            TravelStyle.LUXURY,      Set.of(TravelStyle.COUPLE, TravelStyle.FAMILY),
            TravelStyle.SLOW_TRAVEL, Set.of(TravelStyle.SOLO, TravelStyle.COUPLE, TravelStyle.BACKPACKER),
            TravelStyle.ROAD_TRIP,   Set.of(TravelStyle.ADVENTURE, TravelStyle.FAMILY, TravelStyle.COUPLE),
            TravelStyle.SOLO,        Set.of(TravelStyle.BACKPACKER, TravelStyle.SLOW_TRAVEL, TravelStyle.OFFBEAT),
            TravelStyle.COUPLE,      Set.of(TravelStyle.SLOW_TRAVEL, TravelStyle.LUXURY, TravelStyle.ROAD_TRIP),
            TravelStyle.FAMILY,      Set.of(TravelStyle.ROAD_TRIP, TravelStyle.LUXURY),
            TravelStyle.OFFBEAT,     Set.of(TravelStyle.BACKPACKER, TravelStyle.SOLO, TravelStyle.ADVENTURE),
            TravelStyle.ADVENTURE,   Set.of(TravelStyle.BACKPACKER, TravelStyle.ROAD_TRIP, TravelStyle.OFFBEAT)
    );

    public ScoringResult score(ScoringContext ctx) {
        var matchedMoods = new ArrayList<Mood>();
        double moodScore = scoreMood(ctx, matchedMoods);

        var matchedInterests = new ArrayList<Interest>();
        double interestScore = scoreInterests(ctx, matchedInterests);

        var matchedStyles = new ArrayList<TravelStyle>();
        double travelStyleScore = scoreTravelStyle(ctx, matchedStyles);

        double regionScore = scoreRegion(ctx);
        double durationScore = scoreDuration(ctx);
        double budgetScore = scoreBudget(ctx);
        double seasonScore = scoreSeason(ctx);

        double total = Math.min(ScoringResult.MAX_SCORE,
                moodScore + interestScore + travelStyleScore + regionScore
                        + durationScore + budgetScore + seasonScore);

        return new ScoringResult(
                ctx.destinationId(), total,
                moodScore, interestScore, travelStyleScore,
                regionScore, durationScore, budgetScore, seasonScore,
                List.copyOf(matchedMoods), List.copyOf(matchedInterests), List.copyOf(matchedStyles));
    }

    private double scoreMood(ScoringContext ctx, List<Mood> matchedMoods) {
        Mood requested = ctx.requestedMood();
        if (requested == null) return 15.0; // no preference → neutral score

        // Check profile moods first (explicit recommendation metadata)
        List<Mood> profileMoods = ctx.profileMoods();
        if (profileMoods != null && profileMoods.contains(requested)) {
            matchedMoods.add(requested);
            return 30.0;
        }

        // Check destination-service mood strings
        List<String> destMoods = ctx.destinationMoods();
        Set<String> primary = MOOD_TO_DESTINATION_MOODS.getOrDefault(requested, Set.of());
        Set<String> partial = MOOD_PARTIAL_COMPAT.getOrDefault(requested, Set.of());

        if (destMoods != null) {
            for (String dm : destMoods) {
                if (primary.contains(dm)) {
                    matchedMoods.add(requested);
                    return 30.0;
                }
            }
            for (String dm : destMoods) {
                if (partial.contains(dm)) {
                    return 15.0;
                }
            }
        }
        return 0.0;
    }

    private double scoreInterests(ScoringContext ctx, List<Interest> matched) {
        List<Interest> requested = ctx.requestedInterests();
        if (requested == null || requested.isEmpty()) return 0.0;

        List<String> categories = ctx.destinationCategories();
        List<Interest> profileInterests = ctx.profileInterests();
        double score = 0.0;

        for (Interest interest : requested) {
            boolean matches = false;
            // Check profile interests
            if (profileInterests != null && profileInterests.contains(interest)) {
                matches = true;
            }
            // Check category mapping
            if (!matches && categories != null) {
                Set<String> cats = INTEREST_CATEGORY_MAP.getOrDefault(interest, Set.of());
                for (String cat : categories) {
                    if (cats.contains(cat)) { matches = true; break; }
                }
            }
            if (matches) {
                matched.add(interest);
                score += 5.0;
                if (score >= 25.0) break;
            }
        }
        return Math.min(25.0, score);
    }

    private double scoreTravelStyle(ScoringContext ctx, List<TravelStyle> matched) {
        TravelStyle requested = ctx.requestedTravelStyle();
        if (requested == null) return 0.0;

        List<TravelStyle> profileStyles = ctx.profileTravelStyles();
        if (profileStyles != null && profileStyles.contains(requested)) {
            matched.add(requested);
            return 15.0;
        }
        // Partial compatibility
        Set<TravelStyle> compat = TRAVEL_STYLE_COMPAT.getOrDefault(requested, Set.of());
        if (profileStyles != null) {
            for (TravelStyle s : profileStyles) {
                if (compat.contains(s)) return 8.0;
            }
        }
        return 0.0;
    }

    private double scoreRegion(ScoringContext ctx) {
        String requested = ctx.requestedRegion();
        if (requested == null || requested.isBlank()) return 5.0; // no preference

        List<String> profileRegions = ctx.profileRegions();
        if (profileRegions != null) {
            for (String r : profileRegions) {
                if (r.equalsIgnoreCase(requested)) return 10.0;
                if (r.toLowerCase().contains(requested.toLowerCase())
                        || requested.toLowerCase().contains(r.toLowerCase())) return 7.0;
            }
        }
        return 0.0;
    }

    private double scoreDuration(ScoringContext ctx) {
        Integer requested = ctx.requestedDurationDays();
        if (requested == null) return 5.0;

        int min = ctx.profileDurationMin();
        int max = ctx.profileDurationMax();
        if (max == 0) return 5.0;

        if (requested >= min && requested <= max) return 10.0;
        int slack = max - min;
        int buffer = Math.max(1, slack);
        if (requested >= min - buffer && requested <= max + buffer) return 5.0;
        return 0.0;
    }

    private double scoreBudget(ScoringContext ctx) {
        Integer maxBudget = ctx.requestedMaxBudget();
        if (maxBudget == null) return 5.0;

        int budgetMin = ctx.profileBudgetMin();
        if (budgetMin == 0) return 5.0;

        return maxBudget >= budgetMin ? 5.0 : 0.0;
    }

    private double scoreSeason(ScoringContext ctx) {
        Season requested = ctx.requestedSeason();
        if (requested == null) return 2.5;

        List<Season> profileSeasons = ctx.profileSeasonTags();
        if (profileSeasons == null || profileSeasons.isEmpty()) return 2.5;

        return profileSeasons.contains(requested) ? 5.0 : 0.0;
    }
}
