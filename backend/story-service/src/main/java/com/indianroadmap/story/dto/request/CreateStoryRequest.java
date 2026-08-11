package com.indianroadmap.story.dto.request;

import com.indianroadmap.story.document.StoryDifficulty;
import com.indianroadmap.story.document.StoryLanguage;
import com.indianroadmap.story.document.StoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateStoryRequest(
    @NotBlank String slug,
    @NotBlank String destinationId,
    @NotBlank @Size(max = 300) String title,
    @Size(max = 2000) String shortDescription,
    @NotNull StoryType storyType,
    @NotNull StoryDifficulty difficulty,
    List<StoryLanguage> availableLanguages
) {
}
