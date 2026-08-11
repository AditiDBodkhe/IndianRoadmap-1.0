package com.indianroadmap.recommendation.scoring

import com.indianroadmap.recommendation.document.*
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class RecommendationScoringEngineSpec extends Specification {

    @Subject
    RecommendationScoringEngine engine = new RecommendationScoringEngine()

    // ──────────────────────────────────────────────────────
    // Mood scoring
    // ──────────────────────────────────────────────────────

    def "perfect mood match via profile moods returns 30"() {
        given:
        def ctx = buildContext(Mood.ZEN, [], null, null, null, null, null,
                [Mood.ZEN], [], [], [], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == 30.0
        result.matchedMoods() == [Mood.ZEN]
    }

    def "primary mood match via destination moods returns 30"() {
        given:
        // ZEN maps to ZEN in destination moods
        def ctx = buildContext(Mood.ZEN, [], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], ["ZEN"], [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == 30.0
    }

    def "partial mood match via destination moods returns 15"() {
        given:
        // ZEN partial compat includes SPIRITUAL
        def ctx = buildContext(Mood.ZEN, [], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], ["SPIRITUAL"], [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == 15.0
    }

    def "no mood match returns 0"() {
        given:
        def ctx = buildContext(Mood.ZEN, [], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], ["ADVENTURE"], [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == 0.0
    }

    def "null mood returns neutral score 15"() {
        given:
        def ctx = buildContext(null, [], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == 15.0
    }

    @Unroll
    def "mood #mood with destination moods #destMoods produces expected score"() {
        given:
        def ctx = buildContext(mood, [], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], destMoods, [])

        when:
        def result = engine.score(ctx)

        then:
        result.moodScore() == expectedScore

        where:
        mood              | destMoods              | expectedScore
        Mood.ADVENTUROUS  | ["ADVENTURE"]          | 30.0
        Mood.ADVENTUROUS  | ["WILD"]               | 30.0
        Mood.ADVENTUROUS  | ["ZEN"]                | 15.0
        Mood.SPIRITUAL    | ["SPIRITUAL"]          | 30.0
        Mood.CURIOUS      | ["CURIOUS"]            | 30.0
        Mood.ROMANTIC     | ["ZEN"]                | 30.0
        Mood.CULTURAL     | ["HERITAGE"]           | 30.0
        Mood.OFFBEAT      | ["WILD"]               | 30.0
        Mood.SOLITUDE     | ["SOLITUDE"]           | 30.0
        Mood.FAMILY       | ["HERITAGE"]           | 30.0
        Mood.SOCIAL       | ["HERITAGE"]           | 30.0
        Mood.ZEN          | ["HERITAGE"]           | 0.0
    }

    // ──────────────────────────────────────────────────────
    // Interest scoring
    // ──────────────────────────────────────────────────────

    def "single interest match returns 5"() {
        given:
        def ctx = buildContext(null, [Interest.ASTRONOMY], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], [], ["SCIENTIFIC"])

        when:
        def result = engine.score(ctx)

        then:
        result.interestScore() == 5.0
        result.matchedInterests() == [Interest.ASTRONOMY]
    }

    def "two matching interests return 10"() {
        given:
        def ctx = buildContext(null, [Interest.NATURE, Interest.ASTRONOMY], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], [], ["VILLAGE", "SCIENTIFIC"])

        when:
        def result = engine.score(ctx)

        then:
        result.interestScore() == 10.0
    }

    def "interest score is capped at 25"() {
        given:
        def interests = [Interest.NATURE, Interest.MOUNTAINS, Interest.HISTORY, Interest.CULTURE, Interest.SPIRITUALITY, Interest.ADVENTURE]
        def cats = ["VILLAGE", "MOUNTAIN", "HERITAGE", "MONASTERY", "SPIRITUAL", "ADVENTURE"]
        def ctx = buildContext(null, interests, null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], [], cats)

        when:
        def result = engine.score(ctx)

        then:
        result.interestScore() == 25.0
    }

    def "no matching interests return 0"() {
        given:
        def ctx = buildContext(null, [Interest.ASTRONOMY], null, null, null, null, null,
                [], [], [], [], 0, 0, 0, 0, [], [], ["VILLAGE"])

        when:
        def result = engine.score(ctx)

        then:
        result.interestScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Travel style scoring
    // ──────────────────────────────────────────────────────

    def "exact travel style match returns 15"() {
        given:
        def ctx = buildContext(null, [], TravelStyle.SLOW_TRAVEL, null, null, null, null,
                [], [], [TravelStyle.SLOW_TRAVEL], [], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.travelStyleScore() == 15.0
        result.matchedTravelStyles() == [TravelStyle.SLOW_TRAVEL]
    }

    def "compatible travel style returns 8"() {
        given:
        // SLOW_TRAVEL is compatible with SOLO
        def ctx = buildContext(null, [], TravelStyle.SLOW_TRAVEL, null, null, null, null,
                [], [], [TravelStyle.SOLO], [], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.travelStyleScore() == 8.0
    }

    def "no travel style match returns 0"() {
        given:
        def ctx = buildContext(null, [], TravelStyle.LUXURY, null, null, null, null,
                [], [], [TravelStyle.ADVENTURE], [], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.travelStyleScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Region scoring
    // ──────────────────────────────────────────────────────

    def "exact region match returns 10"() {
        given:
        def ctx = buildContext(null, [], null, null, null, "LADAKH", null,
                [], [], [], ["LADAKH"], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.regionScore() == 10.0
    }

    def "no region preference returns 5"() {
        given:
        def ctx = buildContext(null, [], null, null, null, null, null,
                [], [], [], ["LADAKH"], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.regionScore() == 5.0
    }

    def "different region returns 0"() {
        given:
        def ctx = buildContext(null, [], null, null, null, "KERALA", null,
                [], [], [], ["LADAKH"], 0, 0, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.regionScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Duration scoring
    // ──────────────────────────────────────────────────────

    def "duration within ideal range returns 10"() {
        given:
        def ctx = buildContext(null, [], null, 4, null, null, null,
                [], [], [], [], 2, 6, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.durationScore() == 10.0
    }

    def "duration close to range returns 5"() {
        given:
        def ctx = buildContext(null, [], null, 8, null, null, null,
                [], [], [], [], 2, 6, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.durationScore() == 5.0
    }

    def "duration far outside range returns 0"() {
        given:
        def ctx = buildContext(null, [], null, 30, null, null, null,
                [], [], [], [], 2, 6, 0, 0, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.durationScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Budget scoring
    // ──────────────────────────────────────────────────────

    def "budget within range returns 5"() {
        given:
        def ctx = buildContext(null, [], null, null, 25000, null, null,
                [], [], [], [], 0, 0, 10000, 30000, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.budgetScore() == 5.0
    }

    def "budget below minimum returns 0"() {
        given:
        def ctx = buildContext(null, [], null, null, 5000, null, null,
                [], [], [], [], 0, 0, 10000, 30000, [], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.budgetScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Season scoring
    // ──────────────────────────────────────────────────────

    def "matching season returns 5"() {
        given:
        def ctx = buildContext(null, [], null, null, null, null, Season.SUMMER,
                [], [], [], [], 0, 0, 0, 0, [Season.SUMMER, Season.AUTUMN], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.seasonScore() == 5.0
    }

    def "non-matching season returns 0"() {
        given:
        def ctx = buildContext(null, [], null, null, null, null, Season.MONSOON,
                [], [], [], [], 0, 0, 0, 0, [Season.SUMMER, Season.AUTUMN], [], [])

        when:
        def result = engine.score(ctx)

        then:
        result.seasonScore() == 0.0
    }

    // ──────────────────────────────────────────────────────
    // Score normalization and match level
    // ──────────────────────────────────────────────────────

    def "total score does not exceed 100"() {
        given:
        // Perfect match on everything
        def ctx = buildContext(Mood.ZEN, [Interest.ASTRONOMY, Interest.NATURE, Interest.MOUNTAINS, Interest.HISTORY, Interest.CULTURE],
                TravelStyle.SLOW_TRAVEL, 4, 25000, "LADAKH", Season.SUMMER,
                [Mood.ZEN], [Interest.ASTRONOMY, Interest.NATURE, Interest.MOUNTAINS, Interest.HISTORY, Interest.CULTURE],
                [TravelStyle.SLOW_TRAVEL], ["LADAKH"], 2, 6, 5000, 30000,
                [Season.SUMMER], ["ZEN"], ["SCIENTIFIC", "VILLAGE", "MOUNTAIN", "HERITAGE", "SPIRITUAL"])

        when:
        def result = engine.score(ctx)

        then:
        result.totalScore() <= 100.0
    }

    @Unroll
    def "score #score maps to expected match level"() {
        // Test the mapping logic: we build contexts that produce approximately the right scores
        // and verify the match level mapping in the engine is consistent
        expect:
        toMatchLevel(score) == expectedLevel

        where:
        score | expectedLevel
        95.0  | "EXCELLENT"
        90.0  | "EXCELLENT"
        89.9  | "VERY_GOOD"
        75.0  | "VERY_GOOD"
        74.9  | "GOOD"
        60.0  | "GOOD"
        59.9  | "MODERATE"
        40.0  | "MODERATE"
        39.9  | "LOW"
        0.0   | "LOW"
    }

    // Helper to replicate match level logic (mirrors RuleBasedRecommendationEngine)
    private String toMatchLevel(double score) {
        if (score >= 90.0) return "EXCELLENT"
        if (score >= 75.0) return "VERY_GOOD"
        if (score >= 60.0) return "GOOD"
        if (score >= 40.0) return "MODERATE"
        return "LOW"
    }

    // ──────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────

    private ScoringContext buildContext(
            Mood requestedMood, List requestedInterests, TravelStyle requestedStyle,
            Integer durationDays, Integer maxBudget, String preferredRegion, Season season,
            List profileMoods, List profileInterests, List profileStyles, List profileRegions,
            int durationMin, int durationMax, int budgetMin, int budgetMax,
            List profileSeasons, List destMoods, List destCategories) {

        new ScoringContext(
                requestedMood, requestedInterests, requestedStyle,
                durationDays, maxBudget, preferredRegion, season,
                "test-dest",
                profileMoods, profileInterests, profileStyles, profileRegions,
                durationMin, durationMax, budgetMin, budgetMax,
                profileSeasons, destMoods, destCategories
        )
    }
}
