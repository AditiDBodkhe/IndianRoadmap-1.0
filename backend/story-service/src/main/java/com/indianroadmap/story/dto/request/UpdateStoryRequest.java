package com.indianroadmap.story.dto.request;

import com.indianroadmap.story.document.StoryDifficulty;
import com.indianroadmap.story.document.StoryLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateStoryRequest(
    @NotBlank @Size(max = 300) String title,
    @Size(max = 2000) String shortDescription,
    StoryDifficulty difficulty,
    List<StoryLanguage> availableLanguages
) {
}
