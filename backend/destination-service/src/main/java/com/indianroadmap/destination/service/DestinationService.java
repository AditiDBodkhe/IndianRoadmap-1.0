package com.indianroadmap.destination.service;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Mood;
import com.indianroadmap.destination.dto.request.CreateDestinationRequest;
import com.indianroadmap.destination.dto.request.UpdateDestinationRequest;
import com.indianroadmap.destination.dto.response.DestinationResponse;
import com.indianroadmap.destination.dto.response.DestinationSummaryResponse;
import com.indianroadmap.destination.dto.response.PageResponse;

import java.util.List;

public interface DestinationService {
    DestinationResponse create(CreateDestinationRequest request);
    DestinationResponse findById(String id);
    DestinationResponse findBySlug(String slug);
    PageResponse<DestinationSummaryResponse> search(String state, String region,
            DestinationCategory category, Mood mood, int page, int size);
    DestinationResponse update(String id, UpdateDestinationRequest request);
    void delete(String id);
    List<DestinationSummaryResponse> findNearby(double latitude, double longitude, double radiusMeters);
}
