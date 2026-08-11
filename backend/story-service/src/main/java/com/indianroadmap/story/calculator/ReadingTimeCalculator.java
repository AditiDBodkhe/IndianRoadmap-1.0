package com.indianroadmap.story.calculator;

import com.indianroadmap.story.document.StoryChapterDocument;
import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StoryLanguage;
import com.indianroadmap.story.document.StorySectionDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReadingTimeCalculator {

    public int calculate(StoryDocument story) {
        return calculateReadingTime(story);
    }

    int calculateReadingTime(StoryDocument story) {
        if (story == null) {
            return 1;
        }

        List<StorySectionDocument> englishSections = story.getChapters().stream()
            .flatMap(chapter -> chapter.getSections().stream())
            .filter(section -> section.getLanguage() == StoryLanguage.ENGLISH)
            .toList();

        List<StorySectionDocument> sections = englishSections.isEmpty()
            ? story.getChapters().stream().map(StoryChapterDocument::getSections).flatMap(List::stream).toList()
            : englishSections;

        int wordCount = sections.stream()
            .map(StorySectionDocument::getContent)
            .filter(content -> content != null && !content.isBlank())
            .mapToInt(this::countWords)
            .sum();

        return Math.max(1, (int) Math.ceil(wordCount / 200.0d));
    }

    private int countWords(String content) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\s+").length;
    }
}
