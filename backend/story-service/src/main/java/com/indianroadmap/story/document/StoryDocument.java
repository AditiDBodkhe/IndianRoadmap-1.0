package com.indianroadmap.story.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "stories")
public class StoryDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String destinationId;
    private String title;
    private String shortDescription;
    private StoryType storyType;
    private StoryStatus status;
    private StoryDifficulty difficulty;
    private List<StoryLanguage> availableLanguages;
    private List<StoryChapterDocument> chapters;
    private List<StorySourceDocument> sources;
    private int estimatedReadingTimeMinutes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;

    public StoryDocument() {
        this.status = StoryStatus.DRAFT;
        this.availableLanguages = new ArrayList<>();
        this.chapters = new ArrayList<>();
        this.sources = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public StoryType getStoryType() {
        return storyType;
    }

    public void setStoryType(StoryType storyType) {
        this.storyType = storyType;
    }

    public StoryStatus getStatus() {
        return status;
    }

    public void setStatus(StoryStatus status) {
        this.status = status == null ? StoryStatus.DRAFT : status;
    }

    public StoryDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(StoryDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public List<StoryLanguage> getAvailableLanguages() {
        if (availableLanguages == null) {
            availableLanguages = new ArrayList<>();
        }
        return availableLanguages;
    }

    public void setAvailableLanguages(List<StoryLanguage> availableLanguages) {
        this.availableLanguages = availableLanguages == null ? new ArrayList<>() : new ArrayList<>(availableLanguages);
    }

    public List<StoryChapterDocument> getChapters() {
        if (chapters == null) {
            chapters = new ArrayList<>();
        }
        return chapters;
    }

    public void setChapters(List<StoryChapterDocument> chapters) {
        this.chapters = chapters == null ? new ArrayList<>() : new ArrayList<>(chapters);
    }

    public List<StorySourceDocument> getSources() {
        if (sources == null) {
            sources = new ArrayList<>();
        }
        return sources;
    }

    public void setSources(List<StorySourceDocument> sources) {
        this.sources = sources == null ? new ArrayList<>() : new ArrayList<>(sources);
    }

    public int getEstimatedReadingTimeMinutes() {
        return estimatedReadingTimeMinutes;
    }

    public void setEstimatedReadingTimeMinutes(int estimatedReadingTimeMinutes) {
        this.estimatedReadingTimeMinutes = estimatedReadingTimeMinutes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
