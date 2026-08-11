package com.indianroadmap.story.exception;

public class DuplicateStoryException extends RuntimeException {

    public DuplicateStoryException(String slug) {
        super("Story already exists with slug: " + slug);
    }
}
