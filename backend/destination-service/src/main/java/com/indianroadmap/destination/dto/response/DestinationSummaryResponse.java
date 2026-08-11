package com.indianroadmap.destination.dto.response;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Mood;
import java.util.List;

public record DestinationSummaryResponse(
        String id,
        String slug,
        DestinationNameResponse name,
        String state,
        String region,
        CoordinatesResponse coordinates,
        ElevationResponse elevation,
        List<DestinationCategory> categories,
        List<Mood> moods
) {}
