package com.indianroadmap.story.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateChapterRequest(
    @NotBlank @Size(max = 300) String title,
    @Positive int sequence
) {
}
