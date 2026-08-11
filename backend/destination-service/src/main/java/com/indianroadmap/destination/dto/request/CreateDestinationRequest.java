package com.indianroadmap.destination.dto.request;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Language;
import com.indianroadmap.destination.document.Mood;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateDestinationRequest(
        @NotBlank String slug,
        @NotNull @Valid DestinationNameRequest name,
        @NotBlank String state,
        String district,
        @NotBlank String region,
        String shortDescription,
        String description,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @PositiveOrZero int elevationMeters,
        @PositiveOrZero int elevationFeet,
        List<DestinationCategory> categories,
        List<Mood> moods,
        List<Language> languages,
        List<@Valid HistoricalHighlightRequest> historicalHighlights,
        @Valid CulturalInformationRequest culturalInformation,
        @Valid ArchitectureRequest architecture,
        List<@Valid AttractionRequest> attractions,
        List<@Valid ImageReferenceRequest> images,
        List<@Valid SourceReferenceRequest> sources
) {}
