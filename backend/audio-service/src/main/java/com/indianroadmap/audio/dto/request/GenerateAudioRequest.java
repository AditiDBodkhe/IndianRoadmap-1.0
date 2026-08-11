package com.indianroadmap.audio.dto.request;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.VoiceGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateAudioRequest(

        @NotBlank(message = "storyId must not be blank")
        String storyId,

        @NotBlank(message = "chapterId must not be blank")
        String chapterId,

        @NotBlank(message = "sectionId must not be blank")
        String sectionId,

        @NotNull(message = "language must not be null")
        AudioLanguage language,

        @NotBlank(message = "voiceName must not be blank")
        String voiceName,

        @NotNull(message = "voiceGender must not be null")
        VoiceGender voiceGender,

        @NotNull(message = "format must not be null")
        AudioFormat format
) {}
