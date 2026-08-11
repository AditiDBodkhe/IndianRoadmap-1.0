package com.indianroadmap.audio.repository;

import com.indianroadmap.audio.document.AudioAssetDocument;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.AudioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AudioAssetRepository extends MongoRepository<AudioAssetDocument, String> {

    List<AudioAssetDocument> findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(
            String storyId, String chapterId, String sectionId, AudioLanguage language,
            String voiceName, com.indianroadmap.audio.document.AudioFormat format);

    Page<AudioAssetDocument> findByStoryId(String storyId, Pageable pageable);

    Page<AudioAssetDocument> findByStoryIdAndLanguage(String storyId, AudioLanguage language, Pageable pageable);

    Page<AudioAssetDocument> findByStoryIdAndStatus(String storyId, AudioStatus status, Pageable pageable);

    Page<AudioAssetDocument> findByStoryIdAndLanguageAndStatus(String storyId, AudioLanguage language,
                                                                AudioStatus status, Pageable pageable);

    Page<AudioAssetDocument> findBySectionId(String sectionId, Pageable pageable);

    Page<AudioAssetDocument> findBySectionIdAndLanguage(String sectionId, AudioLanguage language, Pageable pageable);

    Page<AudioAssetDocument> findBySectionIdAndStatus(String sectionId, AudioStatus status, Pageable pageable);

    Page<AudioAssetDocument> findBySectionIdAndLanguageAndStatus(String sectionId, AudioLanguage language,
                                                                   AudioStatus status, Pageable pageable);

    List<AudioAssetDocument> findByStatus(AudioStatus status);

    Optional<AudioAssetDocument> findByContentHash(String contentHash);

    int countByStoryIdAndSectionIdAndLanguage(String storyId, String sectionId, AudioLanguage language);
}
