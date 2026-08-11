package com.indianroadmap.recommendation.service;

import com.indianroadmap.recommendation.dto.request.RecommendationProfileRequest;
import com.indianroadmap.recommendation.dto.request.RecommendationRequest;
import com.indianroadmap.recommendation.dto.response.RecommendationProfileResponse;
import com.indianroadmap.recommendation.dto.response.RecommendationResponse;
import com.indianroadmap.recommendation.document.Mood;

import java.util.List;

public interface RecommendationService {

    List<RecommendationResponse> recommend(RecommendationRequest request);

    List<RecommendationResponse> recommendByMood(Mood mood, int limit);

    List<RecommendationResponse> findSimilarDestinations(String destinationId, int limit);

    RecommendationProfileResponse createProfile(RecommendationProfileRequest request);

    RecommendationProfileResponse getProfile(String destinationId);

    RecommendationProfileResponse updateProfile(String destinationId, RecommendationProfileRequest request);

    void deleteProfile(String destinationId);
}
