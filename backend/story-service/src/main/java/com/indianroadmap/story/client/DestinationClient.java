package com.indianroadmap.story.client;

import com.indianroadmap.story.exception.DestinationNotFoundException;
import com.indianroadmap.story.exception.DestinationServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DestinationClient {

    private final RestClient restClient;

    public DestinationClient(@Qualifier("storyDestinationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DestinationSummary getDestination(String destinationId) {
        try {
            DestinationApiResponse response = restClient.get()
                .uri("/api/v1/destinations/{id}", destinationId)
                .retrieve()
                .body(DestinationApiResponse.class);

            if (response == null || !response.success() || response.data() == null) {
                throw new DestinationServiceUnavailableException("Destination service returned empty response");
            }

            DestinationApiData data = response.data();
            return new DestinationSummary(
                data.id(),
                data.slug(),
                preferredName(data.name(), data.slug()),
                data.state(),
                data.region()
            );
        } catch (DestinationNotFoundException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new DestinationNotFoundException(destinationId);
        } catch (HttpClientErrorException ex) {
            throw new DestinationServiceUnavailableException("Destination service returned client error", ex);
        } catch (ResourceAccessException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        } catch (RestClientResponseException ex) {
            throw new DestinationServiceUnavailableException("Destination service returned server error", ex);
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        }
    }

    public DestinationSummary getDestinationBySlug(String slug) {
        try {
            DestinationApiResponse response = restClient.get()
                .uri("/api/v1/destinations/slug/{slug}", slug)
                .retrieve()
                .body(DestinationApiResponse.class);

            if (response == null || !response.success() || response.data() == null) {
                throw new DestinationServiceUnavailableException("Destination service returned empty response");
            }

            DestinationApiData data = response.data();
            return new DestinationSummary(
                data.id(),
                data.slug(),
                preferredName(data.name(), data.slug()),
                data.state(),
                data.region()
            );
        } catch (DestinationNotFoundException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new DestinationNotFoundException(slug);
        } catch (HttpClientErrorException ex) {
            throw new DestinationServiceUnavailableException("Destination service returned client error", ex);
        } catch (ResourceAccessException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        } catch (RestClientResponseException ex) {
            throw new DestinationServiceUnavailableException("Destination service returned server error", ex);
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        }
    }

    private String preferredName(DestinationNameData name, String slug) {
        if (name == null) {
            return slug;
        }
        if (name.defaultName() != null && !name.defaultName().isBlank()) {
            return name.defaultName();
        }
        if (name.localName() != null && !name.localName().isBlank()) {
            return name.localName();
        }
        return slug;
    }

    record DestinationApiResponse(boolean success, DestinationApiData data) {
    }

    record DestinationApiData(String id, String slug, DestinationNameData name, String state, String region) {
    }

    record DestinationNameData(String defaultName, String localName) {
    }
}
