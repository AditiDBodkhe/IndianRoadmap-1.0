package com.indianroadmap.audio.exception;

public class AudioGenerationException extends RuntimeException {
    public AudioGenerationException(String message) {
        super(message);
    }
    public AudioGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
