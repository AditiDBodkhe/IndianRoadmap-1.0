package com.indianroadmap.recommendation.controller;

import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.dto.request.RecommendationRequest;
import com.indianroadmap.recommendation.dto.response.ApiResponse;
import com.indianroadmap.recommendation.dto.response.RecommendationResponse;
import com.indianroadmap.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendations", description = "Mood-based destination recommendation APIs")
@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Get personalized recommendations based on mood, interests, and travel style")
    @PostMapping
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> recommend(
            @Valid @RequestBody RecommendationRequest request) {
        List<RecommendationResponse> results = recommendationService.recommend(request);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @Operation(summary = "Get top destinations for a specific mood")
    @GetMapping("/mood/{mood}")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> recommendByMood(
            @PathVariable Mood mood,
            @RequestParam(defaultValue = "10") int limit) {
        List<RecommendationResponse> results = recommendationService.recommendByMood(mood, limit);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @Operation(summary = "Find destinations similar to the given destination")
    @GetMapping("/destination/{destinationId}/similar")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> findSimilar(
            @PathVariable String destinationId,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationResponse> results = recommendationService.findSimilarDestinations(destinationId, limit);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
