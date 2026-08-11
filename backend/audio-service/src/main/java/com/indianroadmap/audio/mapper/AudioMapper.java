package com.indianroadmap.audio.mapper;

import com.indianroadmap.audio.document.AudioAssetDocument;
import com.indianroadmap.audio.dto.response.AudioResponse;
import org.springframework.stereotype.Component;

@Component
public class AudioMapper {

    public AudioResponse toResponse(AudioAssetDocument doc) {
        return new AudioResponse(
                doc.getId(),
                doc.getStoryId(),
                doc.getChapterId(),
                doc.getSectionId(),
                doc.getLanguage(),
                doc.getVoiceName(),
                doc.getVoiceGender(),
                doc.getProvider(),
                doc.getFormat(),
                doc.getStatus(),
                doc.getStoragePath(),
                doc.getPublicUrl(),
                doc.getDurationSeconds(),
                doc.getFileSizeBytes(),
                doc.getContentHash(),
                doc.getErrorMessage(),
                doc.getVersion(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getCompletedAt()
        );
    }
}
