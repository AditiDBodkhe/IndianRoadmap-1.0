package com.indianroadmap.audio.provider;

import com.indianroadmap.audio.document.TtsProviderType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Deterministic mock TTS provider for testing and development.
 * Produces stable audio bytes derived from the request content, allowing
 * the full generation pipeline to be exercised without a real TTS provider.
 */
@Component("mockTtsProvider")
public class MockTtsProvider implements TtsProvider {

    private static final int WORDS_PER_SECOND = 3;
    private static final int MIN_BYTES = 128;

    @Override
    public TtsResult generate(TtsRequest request) {
        byte[] audioBytes = deterministicBytes(request);
        double durationSeconds = estimateDuration(request.text());
        return new TtsResult(audioBytes, durationSeconds, TtsProviderType.MOCK);
    }

    private byte[] deterministicBytes(TtsRequest request) {
        String seed = request.text() + "|" + request.language() + "|" + request.voiceName()
                + "|" + request.voiceGender() + "|" + request.format();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            // Expand hash to a realistic mock audio size (1 KB + hash repetitions)
            int targetSize = Math.max(MIN_BYTES, seed.length() * 4);
            byte[] result = new byte[targetSize];
            for (int i = 0; i < targetSize; i++) {
                result[i] = hash[i % hash.length];
            }
            return result;
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is always available in JDK — this branch is unreachable
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private double estimateDuration(String text) {
        if (text == null || text.isBlank()) {
            return 1.0;
        }
        int words = text.trim().split("\\s+").length;
        return Math.max(1.0, (double) words / WORDS_PER_SECOND);
    }
}
