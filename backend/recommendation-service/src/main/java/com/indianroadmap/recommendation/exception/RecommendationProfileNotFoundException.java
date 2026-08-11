package com.indianroadmap.recommendation.exception;

public class RecommendationProfileNotFoundException extends RuntimeException {

    public RecommendationProfileNotFoundException(String destinationId) {
        super("Recommendation profile not found for destination: " + destinationId);
    }
}
