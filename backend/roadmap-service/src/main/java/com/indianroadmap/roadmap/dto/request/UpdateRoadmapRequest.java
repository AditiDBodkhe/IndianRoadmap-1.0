package com.indianroadmap.roadmap.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoadmapRequest(
        @NotBlank(message = "Name must not be blank") String name,
        String description
) {}
