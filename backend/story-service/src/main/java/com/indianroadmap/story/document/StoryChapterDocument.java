package com.indianroadmap.story.document;

import java.util.ArrayList;
import java.util.List;

public class StoryChapterDocument {

    private String chapterId;
    private int sequence;
    private String title;
    private List<StorySectionDocument> sections;

    public StoryChapterDocument() {
        this.sections = new ArrayList<>();
    }

    public StoryChapterDocument(String chapterId, int sequence, String title, List<StorySectionDocument> sections) {
        this.chapterId = chapterId;
        this.sequence = sequence;
        this.title = title;
        this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<StorySectionDocument> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        return sections;
    }

    public void setSections(List<StorySectionDocument> sections) {
        this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
    }
}
