package com.indianroadmap.recommendation.exception;

public class DestinationNotFoundException extends RuntimeException {

    public DestinationNotFoundException(String destinationId) {
        super("Destination not found: " + destinationId);
    }
}
