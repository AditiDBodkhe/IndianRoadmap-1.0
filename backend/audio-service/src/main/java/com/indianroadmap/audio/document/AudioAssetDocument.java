package com.indianroadmap.audio.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audio_assets")
public class AudioAssetDocument {

    @Id
    private String id;

    private String storyId;
    private String chapterId;
    private String sectionId;
    private AudioLanguage language;
    private String voiceName;
    private VoiceGender voiceGender;
    private TtsProviderType provider;
    private AudioFormat format;
    private AudioStatus status;
    private String storagePath;
    private String publicUrl;
    private Double durationSeconds;
    private Long fileSizeBytes;
    private String contentHash;
    private String errorMessage;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public AudioAssetDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public AudioLanguage getLanguage() { return language; }
    public void setLanguage(AudioLanguage language) { this.language = language; }

    public String getVoiceName() { return voiceName; }
    public void setVoiceName(String voiceName) { this.voiceName = voiceName; }

    public VoiceGender getVoiceGender() { return voiceGender; }
    public void setVoiceGender(VoiceGender voiceGender) { this.voiceGender = voiceGender; }

    public TtsProviderType getProvider() { return provider; }
    public void setProvider(TtsProviderType provider) { this.provider = provider; }

    public AudioFormat getFormat() { return format; }
    public void setFormat(AudioFormat format) { this.format = format; }

    public AudioStatus getStatus() { return status; }
    public void setStatus(AudioStatus status) { this.status = status; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public Double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Double durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
