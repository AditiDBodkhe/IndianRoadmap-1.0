package com.indianroadmap.audio.storage;

import com.indianroadmap.audio.config.AudioStorageProperties;
import com.indianroadmap.audio.exception.AudioGenerationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Stores audio files on the local filesystem.
 * Directory structure: {root}/{storyId}/{sectionId}/{language}/audio-v{version}.{ext}
 */
@Component
public class LocalAudioStorage implements AudioStorage {

    private final Path rootPath;

    public LocalAudioStorage(AudioStorageProperties properties) {
        this.rootPath = Path.of(properties.path()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String storyId, String sectionId, String language, String voiceName,
                        int version, String extension, byte[] audioBytes) {
        Path dir = rootPath.resolve(sanitize(storyId))
                .resolve(sanitize(sectionId))
                .resolve(sanitize(language));
        try {
            Files.createDirectories(dir);
            String fileName = "audio-v%d.%s".formatted(version, extension);
            Path filePath = dir.resolve(fileName);
            Files.write(filePath, audioBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return rootPath.relativize(filePath).toString();
        } catch (IOException ex) {
            throw new AudioGenerationException("Failed to store audio file: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        try {
            Path filePath = rootPath.resolve(storagePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log and continue — deletion failure should not block other operations
        }
    }

    @Override
    public byte[] read(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new AudioGenerationException("Storage path is empty");
        }
        Path filePath = rootPath.resolve(storagePath).normalize();
        if (!Files.exists(filePath)) {
            throw new AudioGenerationException("Audio file not found at path: " + storagePath);
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new AudioGenerationException("Failed to read audio file: " + ex.getMessage(), ex);
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
