package com.indianroadmap.roadmap.client;

import com.indianroadmap.roadmap.exception.DestinationNotFoundException;
import com.indianroadmap.roadmap.exception.DestinationServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DestinationClient {

    private final RestClient restClient;

    public DestinationClient(@Qualifier("destinationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public DestinationSummary getDestination(String destinationId) {
        try {
            DestinationApiResponse response = restClient.get()
                .uri("/api/v1/destinations/{id}", destinationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    if (clientResponse.getStatusCode().value() == 404) {
                        throw new DestinationNotFoundException(destinationId);
                    }
                    throw new DestinationServiceUnavailableException("Destination service returned client error");
                })
                .onStatus(HttpStatusCode::is5xxServerError,
                    (request, clientResponse) -> {
                        throw new DestinationServiceUnavailableException("Destination service returned server error");
                    })
                .body(DestinationApiResponse.class);

            if (response == null || response.data() == null) {
                throw new DestinationServiceUnavailableException("Destination service returned empty response");
            }

            String displayName = preferredName(response.data().name(), response.data().slug());
            return new DestinationSummary(
                response.data().id(),
                response.data().slug(),
                displayName,
                response.data().coordinates() == null ? 0.0 : response.data().coordinates().latitude(),
                response.data().coordinates() == null ? 0.0 : response.data().coordinates().longitude(),
                response.data().elevation() == null ? 0 : response.data().elevation().meters()
            );
        } catch (DestinationNotFoundException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new DestinationNotFoundException(destinationId);
        } catch (ResourceAccessException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service call failed", ex);
        }
    }

    public DestinationSummary getDestinationBySlug(String slug) {
        try {
            DestinationApiResponse response = restClient.get()
                .uri("/api/v1/destinations/slug/{slug}", slug)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    if (clientResponse.getStatusCode().value() == 404) {
                        throw new DestinationNotFoundException(slug);
                    }
                    throw new DestinationServiceUnavailableException("Destination service returned client error");
                })
                .onStatus(HttpStatusCode::is5xxServerError,
                    (request, clientResponse) -> {
                        throw new DestinationServiceUnavailableException("Destination service returned server error");
                    })
                .body(DestinationApiResponse.class);

            if (response == null || response.data() == null) {
                throw new DestinationServiceUnavailableException("Destination service returned empty response");
            }

            String displayName = preferredName(response.data().name(), response.data().slug());
            return new DestinationSummary(
                response.data().id(),
                response.data().slug(),
                displayName,
                response.data().coordinates() == null ? 0.0 : response.data().coordinates().latitude(),
                response.data().coordinates() == null ? 0.0 : response.data().coordinates().longitude(),
                response.data().elevation() == null ? 0 : response.data().elevation().meters()
            );
        } catch (DestinationNotFoundException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new DestinationNotFoundException(slug);
        } catch (ResourceAccessException ex) {
            throw new DestinationServiceUnavailableException("Destination service unavailable", ex);
        } catch (RestClientException ex) {
            throw new DestinationServiceUnavailableException("Destination service call failed", ex);
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

    record DestinationApiResponse(boolean success, DestinationApiData data) {}
    record DestinationApiData(String id, String slug, DestinationNameData name,
                              DestinationCoordsData coordinates, DestinationElevationData elevation) {}
    record DestinationNameData(String defaultName, String localName) {}
    record DestinationCoordsData(double latitude, double longitude) {}
    record DestinationElevationData(int meters, int feet) {}
}
