package com.indianroadmap.roadmap.exception;

public class RoadmapEdgeNotFoundException extends RuntimeException {
    public RoadmapEdgeNotFoundException(String edgeId) {
        super("Roadmap edge not found: " + edgeId);
    }
}
