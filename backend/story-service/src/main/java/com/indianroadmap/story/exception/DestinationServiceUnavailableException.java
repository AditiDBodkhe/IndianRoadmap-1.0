package com.indianroadmap.story.exception;

public class DestinationServiceUnavailableException extends RuntimeException {

    public DestinationServiceUnavailableException(String message) {
        super(message);
    }

    public DestinationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
