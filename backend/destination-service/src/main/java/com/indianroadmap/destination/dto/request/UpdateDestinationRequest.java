package com.indianroadmap.destination.dto.request;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Language;
import com.indianroadmap.destination.document.Mood;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record UpdateDestinationRequest(
        String slug,
        @Valid DestinationNameRequest name,
        String state,
        String district,
        String region,
        String shortDescription,
        String description,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @PositiveOrZero Integer elevationMeters,
        @PositiveOrZero Integer elevationFeet,
        List<DestinationCategory> categories,
        List<Mood> moods,
        List<Language> languages,
        List<HistoricalHighlightRequest> historicalHighlights,
        CulturalInformationRequest culturalInformation,
        ArchitectureRequest architecture,
        List<AttractionRequest> attractions,
        List<ImageReferenceRequest> images,
        List<SourceReferenceRequest> sources
) {}
