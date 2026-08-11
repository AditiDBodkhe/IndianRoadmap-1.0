package com.indianroadmap.roadmap.exception;

public class DuplicateRoadmapException extends RuntimeException {
    public DuplicateRoadmapException(String slug) {
        super("Roadmap already exists for slug: " + slug);
    }
}
