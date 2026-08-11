package com.indianroadmap.audio.exception;

public class StoryServiceUnavailableException extends RuntimeException {
    public StoryServiceUnavailableException(String message) {
        super(message);
    }
    public StoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
