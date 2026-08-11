package com.indianroadmap.story.validation;

import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.dto.request.CreateStoryRequest;
import com.indianroadmap.story.exception.InvalidStoryException;
import com.indianroadmap.story.exception.InvalidStoryStatusException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class StoryValidator {

    private static final Set<String> ALLOWED_TRANSITIONS = Set.of(
        "DRAFT->REVIEW",
        "REVIEW->PUBLISHED",
        "DRAFT->ARCHIVED",
        "REVIEW->ARCHIVED",
        "PUBLISHED->ARCHIVED"
    );

    public void validateCreateRequest(CreateStoryRequest request) {
        if (request == null) {
            throw new InvalidStoryException("Story request is required");
        }
        if (request.slug() == null || request.slug().isBlank()) {
            throw new InvalidStoryException("Story slug must not be blank");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidStoryException("Story title must not be blank");
        }
        if (request.storyType() == null) {
            throw new InvalidStoryException("Story type is required");
        }
    }

    public String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    public void validateStatusTransition(StoryStatus from, StoryStatus to) {
        if (from == null || to == null) {
            throw new InvalidStoryStatusException("Story status transition requires both source and target status");
        }
        if (from == to) {
            return;
        }
        String key = from.name() + "->" + to.name();
        if (!ALLOWED_TRANSITIONS.contains(key)) {
            throw new InvalidStoryStatusException("Invalid story status transition: " + from + " -> " + to);
        }
    }
}
