package com.indianroadmap.audio.exception;

public class AudioNotFoundException extends RuntimeException {
    public AudioNotFoundException(String id) {
        super("Audio asset not found: " + id);
    }
}
