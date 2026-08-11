package com.indianroadmap.roadmap.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderRoadmapNodesRequest(
        @NotEmpty(message = "Node IDs must not be empty") List<String> nodeIds
) {}
