package com.indianroadmap.recommendation.engine;

import com.indianroadmap.recommendation.dto.request.RecommendationRequest;
import com.indianroadmap.recommendation.dto.response.RecommendationResponse;

import java.util.List;

/**
 * Abstraction for the recommendation engine.
 * Current implementation: RuleBasedRecommendationEngine.
 * Future: MLRecommendationEngine, LLMRecommendationEngine, HybridRecommendationEngine.
 */
public interface RecommendationEngine {

    List<RecommendationResponse> recommend(RecommendationRequest request);
}
