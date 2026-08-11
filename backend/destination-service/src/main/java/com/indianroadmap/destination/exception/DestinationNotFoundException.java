package com.indianroadmap.destination.exception;

public class DestinationNotFoundException extends RuntimeException {
    public DestinationNotFoundException(String identifier) {
        super("Destination not found: " + identifier);
    }
}
