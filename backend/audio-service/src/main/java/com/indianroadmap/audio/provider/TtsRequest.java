package com.indianroadmap.audio.provider;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.VoiceGender;

public record TtsRequest(
        String text,
        AudioLanguage language,
        String voiceName,
        VoiceGender voiceGender,
        AudioFormat format
) {}
