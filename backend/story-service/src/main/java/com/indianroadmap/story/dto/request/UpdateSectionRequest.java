package com.indianroadmap.story.dto.request;

import com.indianroadmap.story.document.StoryLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSectionRequest(
    @Size(max = 300) String heading,
    @NotBlank @Size(max = 100000) String content,
    @Positive int sequence,
    @NotNull StoryLanguage language
) {
}
