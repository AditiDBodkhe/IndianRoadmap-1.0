package com.indianroadmap.recommendation.engine;

import com.indianroadmap.recommendation.client.DestinationClient;
import com.indianroadmap.recommendation.client.DestinationSummary;
import com.indianroadmap.recommendation.client.StoryClient;
import com.indianroadmap.recommendation.client.StorySummary;
import com.indianroadmap.recommendation.document.MatchLevel;
import com.indianroadmap.recommendation.document.RecommendationProfileDocument;
import com.indianroadmap.recommendation.dto.request.RecommendationRequest;
import com.indianroadmap.recommendation.dto.response.DestinationSummaryDto;
import com.indianroadmap.recommendation.dto.response.RecommendationResponse;
import com.indianroadmap.recommendation.explanation.RecommendationExplanationGenerator;
import com.indianroadmap.recommendation.repository.RecommendationProfileRepository;
import com.indianroadmap.recommendation.scoring.RecommendationScoringEngine;
import com.indianroadmap.recommendation.scoring.ScoringContext;
import com.indianroadmap.recommendation.scoring.ScoringResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule-based deterministic recommendation engine.
 * Scores every known destination against the user request and returns the top-N results.
 */
@Component
public class RuleBasedRecommendationEngine implements RecommendationEngine {

    private final RecommendationProfileRepository profileRepository;
    private final DestinationClient destinationClient;
    private final StoryClient storyClient;
    private final RecommendationScoringEngine scoringEngine;
    private final RecommendationExplanationGenerator explanationGenerator;

    public RuleBasedRecommendationEngine(
            RecommendationProfileRepository profileRepository,
            DestinationClient destinationClient,
            StoryClient storyClient,
            RecommendationScoringEngine scoringEngine,
            RecommendationExplanationGenerator explanationGenerator) {
        this.profileRepository = profileRepository;
        this.destinationClient = destinationClient;
        this.storyClient = storyClient;
        this.scoringEngine = scoringEngine;
        this.explanationGenerator = explanationGenerator;
    }

    @Override
    public List<RecommendationResponse> recommend(RecommendationRequest request) {
        // Fetch all destinations at once (avoids N+1 individual calls)
        List<DestinationSummary> destinations = destinationClient.getAllDestinations();

        // Build profile lookup map
        Map<String, RecommendationProfileDocument> profileMap = profileRepository.findAll().stream()
                .collect(Collectors.toMap(RecommendationProfileDocument::getDestinationId, Function.identity()));

        int limit = request.effectiveLimit();

        return destinations.stream()
                .map(dest -> scoreDestination(dest, profileMap.get(dest.id()), request))
                .sorted(Comparator.comparingDouble(RecommendationResponse::score).reversed()
                        .thenComparing(r -> r.destination().name()))
                .limit(limit)
                .toList();
    }

    private RecommendationResponse scoreDestination(DestinationSummary dest,
                                                     RecommendationProfileDocument profile,
                                                     RecommendationRequest request) {
        ScoringContext ctx = buildContext(dest, profile, request);
        ScoringResult result = scoringEngine.score(ctx);

        // Story enrichment (best-effort)
        List<StorySummary> stories = storyClient.getStoriesForDestination(dest.id());

        List<String> reasons = explanationGenerator.generate(
                result, request.mood(), request.preferredRegion(), stories);

        double roundedScore = BigDecimal.valueOf(result.totalScore())
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
        MatchLevel matchLevel = toMatchLevel(roundedScore);

        DestinationSummaryDto destinationDto = new DestinationSummaryDto(
                dest.id(), dest.slug(), dest.name(), dest.state(), dest.region(),
                dest.categories(), dest.moods());

        return new RecommendationResponse(
                destinationDto, roundedScore, matchLevel, reasons,
                result.matchedMoods(), result.matchedInterests(), result.matchedTravelStyles());
    }

    private ScoringContext buildContext(DestinationSummary dest,
                                        RecommendationProfileDocument profile,
                                        RecommendationRequest request) {
        return new ScoringContext(
                request.mood(),
                request.interests() != null ? request.interests() : List.of(),
                request.travelStyle(),
                request.durationDays(),
                request.maxBudget(),
                request.preferredRegion(),
                request.season(),
                dest.id(),
                profile != null ? profile.getMoods() : List.of(),
                profile != null ? profile.getInterests() : List.of(),
                profile != null ? profile.getTravelStyles() : List.of(),
                profile != null ? profile.getRegions() : List.of(),
                profile != null ? profile.getIdealDurationMin() : 0,
                profile != null ? profile.getIdealDurationMax() : 0,
                profile != null ? profile.getBudgetMin() : 0,
                profile != null ? profile.getBudgetMax() : 0,
                profile != null ? profile.getSeasonTags() : List.of(),
                dest.moods(),
                dest.categories()
        );
    }

    private MatchLevel toMatchLevel(double score) {
        if (score >= 90.0) return MatchLevel.EXCELLENT;
        if (score >= 75.0) return MatchLevel.VERY_GOOD;
        if (score >= 60.0) return MatchLevel.GOOD;
        if (score >= 40.0) return MatchLevel.MODERATE;
        return MatchLevel.LOW;
    }
}
