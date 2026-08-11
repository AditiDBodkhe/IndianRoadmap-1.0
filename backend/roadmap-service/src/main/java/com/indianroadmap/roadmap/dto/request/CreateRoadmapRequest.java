package com.indianroadmap.roadmap.dto.request;

import com.indianroadmap.roadmap.document.RoadmapStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateRoadmapRequest(
        @NotBlank(message = "Slug must not be blank") String slug,
        @NotBlank(message = "Name must not be blank") String name,
        String description,
        RoadmapStatus status
) {}
