package com.indianroadmap.story.dto.response;

import com.indianroadmap.story.document.StoryDifficulty;
import com.indianroadmap.story.document.StoryLanguage;
import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.document.StoryType;

import java.time.Instant;
import java.util.List;

public record StoryResponse(
    String id,
    String slug,
    String destinationId,
    String title,
    String shortDescription,
    StoryType storyType,
    StoryStatus status,
    StoryDifficulty difficulty,
    List<StoryLanguage> availableLanguages,
    List<StoryChapterResponse> chapters,
    int estimatedReadingTimeMinutes,
    Instant createdAt,
    Instant updatedAt,
    Instant publishedAt
) {
}
