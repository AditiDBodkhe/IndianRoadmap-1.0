package com.indianroadmap.recommendation.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class StoryClient {

    private final RestClient restClient;

    public StoryClient(@Qualifier("recStoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches published stories for a destination to enrich recommendation explanations.
     * Returns empty list if story-service is unavailable (graceful degradation).
     */
    public List<StorySummary> getStoriesForDestination(String destinationId) {
        try {
            StoryListResponse response = restClient.get()
                    .uri("/api/v1/stories/destination/{destinationId}?status=PUBLISHED&size=20", destinationId)
                    .retrieve()
                    .body(StoryListResponse.class);
            if (response == null || response.data() == null) return List.of();
            return response.data().stream()
                    .map(s -> new StorySummary(s.id(), s.storyType(), s.title(), s.status()))
                    .toList();
        } catch (RestClientException ex) {
            // Story-service integration is best-effort; don't fail recommendations
            return List.of();
        }
    }

    private record StoryListResponse(boolean success, List<StoryData> data, Object meta) {}

    private record StoryData(String id, String storyType, String title, String status) {}
}
