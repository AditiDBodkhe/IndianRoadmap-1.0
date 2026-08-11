package com.indianroadmap.roadmap.validation;

import com.indianroadmap.roadmap.document.RoadmapStatus;
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest;
import com.indianroadmap.roadmap.exception.InvalidRoadmapException;
import com.indianroadmap.roadmap.exception.InvalidRoadmapStatusException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class RoadmapValidator {

    private static final Set<String> ALLOWED_TRANSITIONS = Set.of(
        "DRAFT->PUBLISHED",
        "DRAFT->ARCHIVED",
        "PUBLISHED->ARCHIVED"
    );

    public void validateCreateRequest(CreateRoadmapRequest request) {
        if (request == null) {
            throw new InvalidRoadmapException("Roadmap request is required");
        }
        if (request.slug() == null || request.slug().isBlank()) {
            throw new InvalidRoadmapException("Roadmap slug must not be blank");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidRoadmapException("Roadmap name must not be blank");
        }
    }

    public void validateStatusTransition(RoadmapStatus from, RoadmapStatus to) {
        if (from == null || to == null) {
            throw new InvalidRoadmapStatusException("Roadmap status transition requires both source and target status");
        }
        if (from == to) {
            return;
        }
        String key = from.name() + "->" + to.name();
        if (!ALLOWED_TRANSITIONS.contains(key)) {
            throw new InvalidRoadmapStatusException("Invalid roadmap status transition: " + from + " -> " + to);
        }
    }

    public String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("\s+", "-");
    }
}
