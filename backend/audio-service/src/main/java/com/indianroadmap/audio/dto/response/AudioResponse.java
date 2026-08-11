package com.indianroadmap.audio.dto.response;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.AudioStatus;
import com.indianroadmap.audio.document.TtsProviderType;
import com.indianroadmap.audio.document.VoiceGender;

import java.time.Instant;

public record AudioResponse(
        String id,
        String storyId,
        String chapterId,
        String sectionId,
        AudioLanguage language,
        String voiceName,
        VoiceGender voiceGender,
        TtsProviderType provider,
        AudioFormat format,
        AudioStatus status,
        String storagePath,
        String publicUrl,
        Double durationSeconds,
        Long fileSizeBytes,
        String contentHash,
        String errorMessage,
        int version,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {}
