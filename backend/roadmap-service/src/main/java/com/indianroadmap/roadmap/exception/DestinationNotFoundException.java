package com.indianroadmap.roadmap.exception;

public class DestinationNotFoundException extends RuntimeException {
    public DestinationNotFoundException(String destinationId) {
        super("Destination not found: " + destinationId);
    }
}
