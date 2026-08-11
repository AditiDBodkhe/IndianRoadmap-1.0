package com.indianroadmap.story.validation;

import com.indianroadmap.story.document.StoryChapterDocument;
import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StorySectionDocument;
import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.dto.request.AddSectionRequest;
import com.indianroadmap.story.exception.InvalidStoryStatusException;
import com.indianroadmap.story.exception.InvalidStoryStructureException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StoryStructureValidator {

    public void validateForReview(StoryDocument story) {
        validateHasReviewableStructure(story);
    }

    public void validateForPublishing(StoryDocument story) {
        if (story.getStatus() != StoryStatus.REVIEW) {
            throw new InvalidStoryStatusException("Story must be in REVIEW status before publishing");
        }
        validateHasReviewableStructure(story);
    }

    public void validateSection(StoryDocument story, StoryChapterDocument chapter, AddSectionRequest request) {
        if (!story.getAvailableLanguages().contains(request.language())) {
            throw new InvalidStoryStructureException("Section language must be one of the story's available languages");
        }
        boolean duplicate = chapter.getSections().stream()
            .anyMatch(section -> section.getSequence() == request.sequence() && section.getLanguage() == request.language());
        if (duplicate) {
            throw new InvalidStoryStructureException("Duplicate section sequence already exists for the same language");
        }
    }

    public void validateChapterSequences(List<StoryChapterDocument> chapters) {
        for (StoryChapterDocument chapter : chapters) {
            if (chapter.getSequence() <= 0) {
                throw new InvalidStoryStructureException("Chapter sequence must be positive");
            }
        }
    }

    public void validateSectionSequences(List<StorySectionDocument> sections) {
        for (StorySectionDocument section : sections) {
            if (section.getSequence() <= 0) {
                throw new InvalidStoryStructureException("Section sequence must be positive");
            }
        }
    }

    private void validateHasReviewableStructure(StoryDocument story) {
        if (story.getChapters().isEmpty()) {
            throw new InvalidStoryStructureException("Story must contain at least one chapter");
        }
        for (StoryChapterDocument chapter : story.getChapters()) {
            if (chapter.getSections().isEmpty()) {
                throw new InvalidStoryStructureException("Each chapter must contain at least one section");
            }
            validateSectionSequences(chapter.getSections());
        }
        validateChapterSequences(story.getChapters());
    }
}
