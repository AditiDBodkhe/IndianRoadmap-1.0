package com.indianroadmap.story.document;

public class StorySectionDocument {

    private String sectionId;
    private int sequence;
    private String heading;
    private String content;
    private StoryLanguage language;

    public StorySectionDocument() {
    }

    public StorySectionDocument(String sectionId, int sequence, String heading, String content, StoryLanguage language) {
        this.sectionId = sectionId;
        this.sequence = sequence;
        this.heading = heading;
        this.content = content;
        this.language = language;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public StoryLanguage getLanguage() {
        return language;
    }

    public void setLanguage(StoryLanguage language) {
        this.language = language;
    }
}
