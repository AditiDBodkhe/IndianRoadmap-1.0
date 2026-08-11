package com.indianroadmap.audio.storage;

public interface AudioStorage {

    /**
     * Stores audio bytes and returns the storage path.
     */
    String store(String storyId, String sectionId, String language, String voiceName,
                 int version, String extension, byte[] audioBytes);

    /**
     * Deletes audio at the given storage path. Handles missing files gracefully.
     */
    void delete(String storagePath);

    /**
     * Reads audio bytes from storage.
     */
    byte[] read(String storagePath);
}
