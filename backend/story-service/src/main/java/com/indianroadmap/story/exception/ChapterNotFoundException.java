package com.indianroadmap.story.exception;

public class ChapterNotFoundException extends RuntimeException {

    public ChapterNotFoundException(String id) {
        super("Chapter not found: " + id);
    }
}
