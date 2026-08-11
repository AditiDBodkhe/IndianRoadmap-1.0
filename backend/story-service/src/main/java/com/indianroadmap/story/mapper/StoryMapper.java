package com.indianroadmap.story.mapper;

import com.indianroadmap.story.document.StoryChapterDocument;
import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StorySectionDocument;
import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.dto.request.CreateStoryRequest;
import com.indianroadmap.story.dto.response.StoryChapterResponse;
import com.indianroadmap.story.dto.response.StoryResponse;
import com.indianroadmap.story.dto.response.StorySectionResponse;
import com.indianroadmap.story.dto.response.StorySummaryResponse;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class StoryMapper {

    public StoryDocument mapToDocument(CreateStoryRequest request, Clock clock) {
        Instant now = Instant.now(clock);
        StoryDocument document = new StoryDocument();
        document.setSlug(normalizeSlug(request.slug()));
        document.setDestinationId(request.destinationId().trim());
        document.setTitle(request.title().trim());
        document.setShortDescription(request.shortDescription());
        document.setStoryType(request.storyType());
        document.setStatus(StoryStatus.DRAFT);
        document.setDifficulty(request.difficulty());
        document.setAvailableLanguages(request.availableLanguages() == null ? List.of() : request.availableLanguages());
        document.setChapters(new ArrayList<>());
        document.setSources(new ArrayList<>());
        document.setEstimatedReadingTimeMinutes(1);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return document;
    }

    public StoryResponse mapToResponse(StoryDocument story) {
        return new StoryResponse(
            story.getId(),
            story.getSlug(),
            story.getDestinationId(),
            story.getTitle(),
            story.getShortDescription(),
            story.getStoryType(),
            story.getStatus(),
            story.getDifficulty(),
            List.copyOf(story.getAvailableLanguages()),
            story.getChapters().stream()
                .sorted(Comparator.comparingInt(StoryChapterDocument::getSequence))
                .map(this::mapChapterToResponse)
                .toList(),
            story.getEstimatedReadingTimeMinutes(),
            story.getCreatedAt(),
            story.getUpdatedAt(),
            story.getPublishedAt()
        );
    }

    public StorySummaryResponse mapToSummaryResponse(StoryDocument story) {
        return new StorySummaryResponse(
            story.getId(),
            story.getSlug(),
            story.getDestinationId(),
            story.getTitle(),
            story.getShortDescription(),
            story.getStoryType(),
            story.getStatus(),
            story.getDifficulty(),
            List.copyOf(story.getAvailableLanguages()),
            story.getEstimatedReadingTimeMinutes(),
            story.getCreatedAt(),
            story.getUpdatedAt()
        );
    }

    public StoryChapterResponse mapChapterToResponse(StoryChapterDocument chapter) {
        return new StoryChapterResponse(
            chapter.getChapterId(),
            chapter.getSequence(),
            chapter.getTitle(),
            chapter.getSections().stream()
                .sorted(Comparator.comparingInt(StorySectionDocument::getSequence))
                .map(this::mapSectionToResponse)
                .toList()
        );
    }

    public StorySectionResponse mapSectionToResponse(StorySectionDocument section) {
        return new StorySectionResponse(
            section.getSectionId(),
            section.getSequence(),
            section.getHeading(),
            section.getContent(),
            section.getLanguage()
        );
    }

    private String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        return slug.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}
