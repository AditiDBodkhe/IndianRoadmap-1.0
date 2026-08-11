package com.indianroadmap.audio.dto.request;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.VoiceGender;

public record UpdateAudioMetadataRequest(
        String voiceName,
        VoiceGender voiceGender,
        AudioFormat format
) {}
