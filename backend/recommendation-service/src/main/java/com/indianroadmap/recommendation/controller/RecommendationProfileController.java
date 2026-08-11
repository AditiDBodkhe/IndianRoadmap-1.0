package com.indianroadmap.recommendation.controller;

import com.indianroadmap.recommendation.dto.request.RecommendationProfileRequest;
import com.indianroadmap.recommendation.dto.response.ApiResponse;
import com.indianroadmap.recommendation.dto.response.RecommendationProfileResponse;
import com.indianroadmap.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recommendation Profiles", description = "Manage destination recommendation metadata")
@RestController
@RequestMapping("/api/v1/recommendation-profiles")
public class RecommendationProfileController {

    private final RecommendationService recommendationService;

    public RecommendationProfileController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Create a recommendation profile for a destination")
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationProfileResponse>> createProfile(
            @Valid @RequestBody RecommendationProfileRequest request) {
        var response = recommendationService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "Get recommendation profile for a destination")
    @GetMapping("/{destinationId}")
    public ResponseEntity<ApiResponse<RecommendationProfileResponse>> getProfile(
            @PathVariable String destinationId) {
        var response = recommendationService.getProfile(destinationId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Update recommendation profile for a destination")
    @PutMapping("/{destinationId}")
    public ResponseEntity<ApiResponse<RecommendationProfileResponse>> updateProfile(
            @PathVariable String destinationId,
            @Valid @RequestBody RecommendationProfileRequest request) {
        var response = recommendationService.updateProfile(destinationId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Delete recommendation profile for a destination")
    @DeleteMapping("/{destinationId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String destinationId) {
        recommendationService.deleteProfile(destinationId);
        return ResponseEntity.noContent().build();
    }
}
