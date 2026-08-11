package com.indianroadmap.roadmap.exception;

public class RoadmapNotFoundException extends RuntimeException {
    public RoadmapNotFoundException(String roadmapId) {
        super("Roadmap not found: " + roadmapId);
    }
}
