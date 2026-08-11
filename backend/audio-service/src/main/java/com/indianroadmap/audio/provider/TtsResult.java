package com.indianroadmap.audio.provider;

import com.indianroadmap.audio.document.TtsProviderType;

public record TtsResult(
        byte[] audioBytes,
        double durationSeconds,
        TtsProviderType provider
) {}
