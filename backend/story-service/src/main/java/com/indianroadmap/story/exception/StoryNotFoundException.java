package com.indianroadmap.story.exception;

public class StoryNotFoundException extends RuntimeException {

    public StoryNotFoundException(String id) {
        super("Story not found: " + id);
    }
}
