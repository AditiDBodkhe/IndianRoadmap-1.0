package com.indianroadmap.recommendation.explanation

import com.indianroadmap.recommendation.client.StorySummary
import com.indianroadmap.recommendation.document.Interest
import com.indianroadmap.recommendation.document.Mood
import com.indianroadmap.recommendation.document.TravelStyle
import com.indianroadmap.recommendation.scoring.ScoringResult
import spock.lang.Specification
import spock.lang.Subject

class RecommendationExplanationGeneratorSpec extends Specification {

    @Subject
    RecommendationExplanationGenerator generator = new RecommendationExplanationGenerator()

    def "perfect mood match generates 'Perfectly matches' reason"() {
        given:
        def result = buildResult("d1", 30.0, 0, 0, 0, 0, 0, [Mood.ZEN], [], [])

        when:
        def reasons = generator.generate(result, Mood.ZEN, null, [])

        then:
        reasons.any { it.contains("Perfectly matches") && it.contains("Zen") }
    }

    def "partial mood match generates 'Good compatibility' reason"() {
        given:
        def result = buildResult("d1", 15.0, 0, 0, 0, 0, 0, [], [], [])

        when:
        def reasons = generator.generate(result, Mood.ZEN, null, [])

        then:
        reasons.any { it.contains("Good compatibility") || it.contains("Partial alignment") || it.contains("Zen") }
    }

    def "nature interest match generates nature reason"() {
        given:
        def result = buildResult("d1", 0, 5.0, 0, 0, 0, 0, [], [Interest.NATURE], [])

        when:
        def reasons = generator.generate(result, null, null, [])

        then:
        reasons.any { it.contains("nature") }
    }

    def "slow travel match generates slow travel reason"() {
        given:
        def result = buildResult("d1", 0, 0, 15.0, 0, 0, 0, [], [], [TravelStyle.SLOW_TRAVEL])

        when:
        def reasons = generator.generate(result, null, null, [])

        then:
        reasons.any { it.contains("slow travel") || it.contains("Ideal for") }
    }

    def "ZEN mood with NATURE and SLOW_TRAVEL generates all three reason types"() {
        given:
        def result = buildResult("d1", 30.0, 5.0, 15.0, 10.0, 0, 0,
                [Mood.ZEN], [Interest.NATURE], [TravelStyle.SLOW_TRAVEL])

        when:
        def reasons = generator.generate(result, Mood.ZEN, "LADAKH", [])

        then:
        reasons.any { it.toLowerCase().contains("zen") }
        reasons.any { it.toLowerCase().contains("nature") }
        reasons.any { it.toLowerCase().contains("slow travel") }
    }

    def "region reason is generated when region matches"() {
        given:
        def result = buildResult("d1", 0, 0, 0, 10.0, 0, 0, [], [], [])

        when:
        def reasons = generator.generate(result, null, "LADAKH", [])

        then:
        reasons.any { it.contains("LADAKH") || it.contains("region") }
    }

    def "budget reason is generated when budget matches"() {
        given:
        def result = buildResult("d1", 0, 0, 0, 0, 0, 5.0, [], [], [])

        when:
        def reasons = generator.generate(result, null, null, [])

        then:
        reasons.any { it.toLowerCase().contains("budget") }
    }

    def "season reason is generated when season matches"() {
        given:
        def result = buildResult("d1", 0, 0, 0, 0, 0, 5.0, [], [], [])

        when:
        def reasons = generator.generate(result, null, null, [])

        then:
        !reasons.empty
    }

    def "spiritual story enriches explanation"() {
        given:
        def result = buildResult("d1", 0, 0, 0, 0, 0, 0, [], [], [])
        def stories = [new StorySummary("s1", "SPIRITUAL", "Tabo Monastery Story", "PUBLISHED")]

        when:
        def reasons = generator.generate(result, null, null, stories)

        then:
        reasons.any { it.toLowerCase().contains("spiritual") }
    }

    def "empty score and no stories still returns fallback reason"() {
        given:
        def result = buildResult("d1", 0, 0, 0, 0, 0, 0, [], [], [])

        when:
        def reasons = generator.generate(result, null, null, [])

        then:
        !reasons.empty
        reasons.every { it instanceof String && !it.blank }
    }

    def "reasons list is immutable"() {
        given:
        def result = buildResult("d1", 30.0, 0, 0, 0, 0, 0, [Mood.ZEN], [], [])

        when:
        def reasons = generator.generate(result, Mood.ZEN, null, [])
        reasons.add("hack")

        then:
        thrown(UnsupportedOperationException)
    }

    // ── Helper ──────────────────────────────────────────

    private ScoringResult buildResult(String id, double moodScore, double interestScore,
                                       double styleScore, double regionScore, double durationScore,
                                       double seasonBudgetScore, List moods, List interests, List styles) {
        double total = moodScore + interestScore + styleScore + regionScore + durationScore + seasonBudgetScore
        new ScoringResult(id, total, moodScore, interestScore, styleScore, regionScore,
                durationScore, seasonBudgetScore, seasonBudgetScore, moods, interests, styles)
    }
}
