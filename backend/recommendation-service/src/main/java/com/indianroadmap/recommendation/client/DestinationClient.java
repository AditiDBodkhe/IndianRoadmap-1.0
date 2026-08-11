package com.indianroadmap.recommendation.client;

import com.indianroadmap.recommendation.exception.DestinationServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DestinationClient {

    private final RestClient restClient;

    public DestinationClient(@Qualifier("recDestinationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Fetch all destinations (used for bulk recommendation scoring). */
    public List<DestinationSummary> getAllDestinations() {
        try {
            DestinationListResponse response = restClient.get()
                    .uri("/api/v1/destinations?size=100")
                    .retrieve()
                    .body(DestinationListResponse.class);
            if (response == null || response.data() == null) return List.of();
            return response.data().stream().map(this::toSummary).toList();
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        }
    }

    /** Fetch a single destination by slug. Returns empty if not found. */
    public Optional<DestinationSummary> getDestination(String slug) {
        try {
            DestinationDetailResponse response = restClient.get()
                    .uri("/api/v1/destinations/slug/{slug}", slug)
                    .retrieve()
                    .body(DestinationDetailResponse.class);
            if (response == null || !response.success() || response.data() == null) {
                return Optional.empty();
            }
            return Optional.of(toSummary(response.data()));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        }
    }

    private DestinationSummary toSummary(DestinationData d) {
        String name = resolveDisplayName(d.name(), d.slug());
        List<String> categories = d.categories() != null ? List.copyOf(d.categories()) : List.of();
        List<String> moods = d.moods() != null ? List.copyOf(d.moods()) : List.of();
        return new DestinationSummary(d.id(), d.slug(), name, d.state(), d.region(), categories, moods);
    }

    private String resolveDisplayName(Map<String, String> name, String slug) {
        if (name == null) return slug;
        String primary = name.get("defaultName");
        if (primary != null && !primary.isBlank()) return primary;
        String english = name.get("localName");
        if (english != null && !english.isBlank()) return english;
        return slug;
    }

    // Inner response records
    private record DestinationListResponse(
            boolean success,
            List<DestinationData> data,
            Object meta) {}

    private record DestinationDetailResponse(
            boolean success,
            DestinationData data) {}

    private record DestinationData(
            String id,
            String slug,
            Map<String, String> name,
            String state,
            String region,
            List<String> categories,
            List<String> moods) {}
}
