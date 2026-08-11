package com.indianroadmap.destination.exception;

public class DuplicateDestinationException extends RuntimeException {
    public DuplicateDestinationException(String slug) {
        super("Destination with slug '" + slug + "' already exists");
    }
}
