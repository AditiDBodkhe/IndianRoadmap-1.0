package com.indianroadmap.story.exception;

public class DestinationNotFoundException extends RuntimeException {

    public DestinationNotFoundException(String id) {
        super("Destination not found: " + id);
    }
}
