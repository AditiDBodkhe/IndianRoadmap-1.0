package com.indianroadmap.audio.provider;

import com.indianroadmap.audio.config.AudioTtsProperties;
import com.indianroadmap.audio.document.TtsProviderType;
import org.springframework.stereotype.Component;

/**
 * Selects the TtsProvider implementation based on configuration.
 * Adding a new provider only requires implementing TtsProvider and
 * registering it here — AudioServiceImpl is not modified.
 */
@Component
public class TtsProviderFactory {

    private final TtsProvider mockProvider;
    private final TtsProviderType configuredProvider;

    public TtsProviderFactory(
            MockTtsProvider mockProvider,
            AudioTtsProperties ttsProperties) {
        this.mockProvider = mockProvider;
        this.configuredProvider = parseProvider(ttsProperties.provider());
    }

    public TtsProvider get() {
        return switch (configuredProvider) {
            case MOCK -> mockProvider;
            case GOOGLE, AZURE, AWS ->
                throw new UnsupportedOperationException(
                        "Provider %s is not yet implemented. Use MOCK for now.".formatted(configuredProvider));
        };
    }

    private TtsProviderType parseProvider(String value) {
        if (value == null || value.isBlank()) {
            return TtsProviderType.MOCK;
        }
        try {
            return TtsProviderType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TtsProviderType.MOCK;
        }
    }
}
