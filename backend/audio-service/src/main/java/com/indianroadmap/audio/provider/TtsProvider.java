package com.indianroadmap.audio.provider;

public interface TtsProvider {

    TtsResult generate(TtsRequest request);
}
