package com.indianroadmap.roadmap.dto.request;

import com.indianroadmap.roadmap.document.RoadmapNodeRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateRoadmapNodeRequest(
        @Positive(message = "Sequence must be positive") int sequence,
        @NotBlank(message = "Label must not be blank") String label,
        @NotNull(message = "Role must not be null") RoadmapNodeRole role
) {}
