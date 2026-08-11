package com.indianroadmap.story.exception;

public class SectionNotFoundException extends RuntimeException {

    public SectionNotFoundException(String id) {
        super("Section not found: " + id);
    }
}
