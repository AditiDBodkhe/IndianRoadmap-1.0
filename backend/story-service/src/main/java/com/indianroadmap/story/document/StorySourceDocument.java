package com.indianroadmap.story.document;

import java.time.Instant;

public class StorySourceDocument {

    private String title;
    private String publisher;
    private String url;
    private SourceType sourceType;
    private boolean verified;
    private Instant verifiedAt;

    public StorySourceDocument() {
    }

    public StorySourceDocument(String title, String publisher, String url, SourceType sourceType, boolean verified, Instant verifiedAt) {
        this.title = title;
        this.publisher = publisher;
        this.url = url;
        this.sourceType = sourceType;
        this.verified = verified;
        this.verifiedAt = verifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
