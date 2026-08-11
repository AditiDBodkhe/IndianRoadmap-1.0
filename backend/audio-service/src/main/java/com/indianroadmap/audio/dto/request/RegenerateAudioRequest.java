package com.indianroadmap.audio.dto.request;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.VoiceGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegenerateAudioRequest(

        @NotBlank(message = "voiceName must not be blank")
        String voiceName,

        @NotNull(message = "voiceGender must not be null")
        VoiceGender voiceGender,

        @NotNull(message = "format must not be null")
        AudioFormat format
) {}
