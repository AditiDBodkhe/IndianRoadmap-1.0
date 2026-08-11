package com.indianroadmap.destination.dto.response;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Language;
import com.indianroadmap.destination.document.Mood;
import java.time.Instant;
import java.util.List;

public record DestinationResponse(
        String id,
        String slug,
        DestinationNameResponse name,
        String state,
        String district,
        String region,
        String shortDescription,
        String description,
        CoordinatesResponse coordinates,
        ElevationResponse elevation,
        List<DestinationCategory> categories,
        List<Mood> moods,
        List<Language> languages,
        List<HistoricalHighlightResponse> historicalHighlights,
        CulturalInformationResponse culturalInformation,
        ArchitectureResponse architecture,
        List<AttractionResponse> attractions,
        List<ImageReferenceResponse> images,
        List<SourceReferenceResponse> sources,
        boolean verified,
        Instant lastVerifiedAt,
        Instant createdAt,
        Instant updatedAt
) {}
