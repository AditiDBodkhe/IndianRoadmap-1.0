package com.indianroadmap.roadmap.exception;

public class RoadmapNodeNotFoundException extends RuntimeException {
    public RoadmapNodeNotFoundException(String nodeId) {
        super("Roadmap node not found: " + nodeId);
    }
}
