package com.indianroadmap.recommendation.service;

import com.indianroadmap.recommendation.client.DestinationClient;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.dto.request.RecommendationProfileRequest;
import com.indianroadmap.recommendation.dto.request.RecommendationRequest;
import com.indianroadmap.recommendation.dto.response.RecommendationProfileResponse;
import com.indianroadmap.recommendation.dto.response.RecommendationResponse;
import com.indianroadmap.recommendation.engine.RecommendationEngine;
import com.indianroadmap.recommendation.exception.DestinationNotFoundException;
import com.indianroadmap.recommendation.exception.RecommendationProfileNotFoundException;
import com.indianroadmap.recommendation.mapper.RecommendationMapper;
import com.indianroadmap.recommendation.repository.RecommendationProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationEngine engine;
    private final RecommendationProfileRepository profileRepository;
    private final DestinationClient destinationClient;
    private final RecommendationMapper mapper;

    public RecommendationServiceImpl(
            RecommendationEngine engine,
            RecommendationProfileRepository profileRepository,
            DestinationClient destinationClient,
            RecommendationMapper mapper) {
        this.engine = engine;
        this.profileRepository = profileRepository;
        this.destinationClient = destinationClient;
        this.mapper = mapper;
    }

    @Override
    public List<RecommendationResponse> recommend(RecommendationRequest request) {
        return engine.recommend(request);
    }

    @Override
    public List<RecommendationResponse> recommendByMood(Mood mood, int limit) {
        var request = new RecommendationRequest(mood, null, null, null, null, null, null, limit);
        return engine.recommend(request);
    }

    @Override
    public List<RecommendationResponse> findSimilarDestinations(String destinationId, int limit) {
        // Find the profile of the reference destination
        var referenceProfile = profileRepository.findByDestinationId(destinationId)
                .orElse(null);

        if (referenceProfile == null) {
            // Fall back to destination-service for its moods
            var dest = destinationClient.getDestination(destinationId)
                    .orElseThrow(() -> new DestinationNotFoundException(destinationId));

            // Build a minimal recommendation request from destination's moods
            Mood mood = dest.moods().isEmpty() ? null : mapDestMoodToRequestMood(dest.moods().get(0));
            var request = new RecommendationRequest(mood, null, null, null, null, dest.region(), null, limit + 1);
            return engine.recommend(request).stream()
                    .filter(r -> !r.destination().slug().equals(destinationId)
                            && !r.destination().id().equals(destinationId))
                    .limit(limit)
                    .toList();
        }

        // Build request from reference profile
        Mood primaryMood = referenceProfile.getMoods().isEmpty() ? null
                : referenceProfile.getMoods().get(0);
        var request = new RecommendationRequest(
                primaryMood,
                referenceProfile.getInterests(),
                referenceProfile.getTravelStyles().isEmpty() ? null
                        : referenceProfile.getTravelStyles().get(0),
                null, null,
                referenceProfile.getRegions().isEmpty() ? null
                        : referenceProfile.getRegions().get(0),
                null, limit + 1);

        return engine.recommend(request).stream()
                .filter(r -> !r.destination().slug().equals(destinationId)
                        && !r.destination().id().equals(destinationId))
                .limit(limit)
                .toList();
    }

    @Override
    public RecommendationProfileResponse createProfile(RecommendationProfileRequest request) {
        // Verify destination exists
        destinationClient.getDestination(request.destinationId())
                .orElseThrow(() -> new DestinationNotFoundException(request.destinationId()));

        var doc = mapper.toDocument(request);
        var saved = profileRepository.save(doc);
        return mapper.toResponse(saved);
    }

    @Override
    public RecommendationProfileResponse getProfile(String destinationId) {
        var doc = profileRepository.findByDestinationId(destinationId)
                .orElseThrow(() -> new RecommendationProfileNotFoundException(destinationId));
        return mapper.toResponse(doc);
    }

    @Override
    public RecommendationProfileResponse updateProfile(String destinationId, RecommendationProfileRequest request) {
        var doc = profileRepository.findByDestinationId(destinationId)
                .orElseThrow(() -> new RecommendationProfileNotFoundException(destinationId));
        mapper.updateDocument(doc, request);
        var saved = profileRepository.save(doc);
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteProfile(String destinationId) {
        if (!profileRepository.existsByDestinationId(destinationId)) {
            throw new RecommendationProfileNotFoundException(destinationId);
        }
        profileRepository.deleteByDestinationId(destinationId);
    }

    private Mood mapDestMoodToRequestMood(String destMood) {
        return switch (destMood) {
            case "ZEN" -> Mood.ZEN;
            case "ADVENTURE" -> Mood.ADVENTUROUS;
            case "SPIRITUAL" -> Mood.SPIRITUAL;
            case "CURIOUS" -> Mood.CURIOUS;
            case "HERITAGE" -> Mood.CULTURAL;
            case "SOLITUDE" -> Mood.SOLITUDE;
            case "WILD" -> Mood.OFFBEAT;
            case "PATRIOTIC" -> Mood.CULTURAL;
            default -> Mood.CURIOUS;
        };
    }
}
