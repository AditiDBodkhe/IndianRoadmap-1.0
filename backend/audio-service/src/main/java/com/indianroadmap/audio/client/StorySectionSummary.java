package com.indianroadmap.audio.client;

import com.indianroadmap.audio.document.AudioLanguage;

public record StorySectionSummary(
        String sectionId,
        int sequence,
        String heading,
        String content,
        AudioLanguage language
) {}
