package com.indianroadmap.audio.service;

import com.indianroadmap.audio.client.StoryClient;
import com.indianroadmap.audio.client.StorySectionSummary;
import com.indianroadmap.audio.document.AudioAssetDocument;
import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.AudioStatus;
import com.indianroadmap.audio.document.TtsProviderType;
import com.indianroadmap.audio.dto.request.GenerateAudioRequest;
import com.indianroadmap.audio.dto.request.RegenerateAudioRequest;
import com.indianroadmap.audio.dto.response.AudioResponse;
import com.indianroadmap.audio.dto.response.PageResponse;
import com.indianroadmap.audio.exception.AudioAlreadyExistsException;
import com.indianroadmap.audio.exception.AudioGenerationException;
import com.indianroadmap.audio.exception.AudioNotFoundException;
import com.indianroadmap.audio.exception.UnsupportedLanguageException;
import com.indianroadmap.audio.mapper.AudioMapper;
import com.indianroadmap.audio.provider.TtsProviderFactory;
import com.indianroadmap.audio.provider.TtsRequest;
import com.indianroadmap.audio.provider.TtsResult;
import com.indianroadmap.audio.repository.AudioAssetRepository;
import com.indianroadmap.audio.storage.AudioStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class AudioServiceImpl implements AudioService {

    private final AudioAssetRepository repository;
    private final StoryClient storyClient;
    private final AudioMapper mapper;
    private final TtsProviderFactory ttsProviderFactory;
    private final AudioStorage audioStorage;
    private final Clock clock;

    @Autowired
    public AudioServiceImpl(AudioAssetRepository repository, StoryClient storyClient,
                             AudioMapper mapper, TtsProviderFactory ttsProviderFactory,
                             AudioStorage audioStorage, Clock clock) {
        this.repository = repository;
        this.storyClient = storyClient;
        this.mapper = mapper;
        this.ttsProviderFactory = ttsProviderFactory;
        this.audioStorage = audioStorage;
        this.clock = clock;
    }

    @Override
    public AudioResponse generateAudio(GenerateAudioRequest request) {
        // Fetch section content from story-service (validates story/chapter/section existence)
        StorySectionSummary section = storyClient.getSection(
                request.storyId(), request.chapterId(), request.sectionId());

        // Validate section language matches requested language
        if (section.language() != request.language()) {
            throw new UnsupportedLanguageException(
                    "Section language is %s but requested language is %s"
                            .formatted(section.language(), request.language()));
        }

        // Reject empty content
        if (section.content() == null || section.content().isBlank()) {
            throw new AudioGenerationException(
                    "Section content is empty — cannot generate audio for an empty section");
        }

        // Check for exact duplicate (same voice/format already completed)
        List<AudioAssetDocument> existingForVoice = repository
                .findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(
                        request.storyId(), request.chapterId(), request.sectionId(),
                        request.language(), request.voiceName(), request.format());
        boolean completedExists = existingForVoice.stream()
                .anyMatch(d -> d.getStatus() == AudioStatus.COMPLETED);
        if (completedExists) {
            throw new AudioAlreadyExistsException(
                    "Completed audio already exists for sectionId=%s language=%s voiceName=%s format=%s. Use /regenerate to create a new version."
                            .formatted(request.sectionId(), request.language(),
                                    request.voiceName(), request.format()));
        }

        // Next version = existing count for this combination + 1
        int version = existingForVoice.size() + 1;

        // Create document in REQUESTED state
        Instant now = clock.instant();
        AudioAssetDocument doc = new AudioAssetDocument();
        doc.setStoryId(request.storyId());
        doc.setChapterId(request.chapterId());
        doc.setSectionId(request.sectionId());
        doc.setLanguage(request.language());
        doc.setVoiceName(request.voiceName());
        doc.setVoiceGender(request.voiceGender());
        doc.setFormat(request.format());
        doc.setStatus(AudioStatus.REQUESTED);
        doc.setVersion(version);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        doc = repository.save(doc);

        // Generate audio
        try {
            doc.setStatus(AudioStatus.GENERATING);
            doc.setUpdatedAt(clock.instant());
            doc = repository.save(doc);

            String contentHash = computeContentHash(section.content(), request.language(),
                    request.voiceName(), request.format());
            doc.setContentHash(contentHash);

            TtsRequest ttsRequest = new TtsRequest(
                    section.content(), request.language(), request.voiceName(),
                    request.voiceGender(), request.format());
            TtsResult result = ttsProviderFactory.get().generate(ttsRequest);

            String storagePath = audioStorage.store(
                    request.storyId(), request.sectionId(),
                    request.language().name().toLowerCase(),
                    request.voiceName(), version,
                    request.format().extension(), result.audioBytes());

            doc.setStoragePath(storagePath);
            doc.setDurationSeconds(result.durationSeconds());
            doc.setFileSizeBytes((long) result.audioBytes().length);
            doc.setProvider(result.provider());
            doc.setStatus(AudioStatus.COMPLETED);
            Instant completed = clock.instant();
            doc.setUpdatedAt(completed);
            doc.setCompletedAt(completed);
            return mapper.toResponse(repository.save(doc));

        } catch (Exception ex) {
            doc.setStatus(AudioStatus.FAILED);
            doc.setErrorMessage(ex.getMessage());
            doc.setUpdatedAt(clock.instant());
            repository.save(doc);
            if (ex instanceof AudioGenerationException age) {
                throw age;
            }
            throw new AudioGenerationException("Audio generation failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public AudioResponse getAudio(String audioId) {
        return repository.findById(audioId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new AudioNotFoundException(audioId));
    }

    @Override
    public PageResponse<AudioResponse> listByStory(String storyId, AudioLanguage language,
                                                    AudioStatus status, Pageable pageable) {
        Page<AudioAssetDocument> page;
        if (language != null && status != null) {
            page = repository.findByStoryIdAndLanguageAndStatus(storyId, language, status, pageable);
        } else if (language != null) {
            page = repository.findByStoryIdAndLanguage(storyId, language, pageable);
        } else if (status != null) {
            page = repository.findByStoryIdAndStatus(storyId, status, pageable);
        } else {
            page = repository.findByStoryId(storyId, pageable);
        }
        List<AudioResponse> items = page.getContent().stream().map(mapper::toResponse).toList();
        return PageResponse.of(items, pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    public PageResponse<AudioResponse> listBySection(String sectionId, AudioLanguage language,
                                                      AudioStatus status, Pageable pageable) {
        Page<AudioAssetDocument> page;
        if (language != null && status != null) {
            page = repository.findBySectionIdAndLanguageAndStatus(sectionId, language, status, pageable);
        } else if (language != null) {
            page = repository.findBySectionIdAndLanguage(sectionId, language, pageable);
        } else if (status != null) {
            page = repository.findBySectionIdAndStatus(sectionId, status, pageable);
        } else {
            page = repository.findBySectionId(sectionId, pageable);
        }
        List<AudioResponse> items = page.getContent().stream().map(mapper::toResponse).toList();
        return PageResponse.of(items, pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    public AudioResponse regenerateAudio(String audioId, RegenerateAudioRequest request) {
        AudioAssetDocument original = repository.findById(audioId)
                .orElseThrow(() -> new AudioNotFoundException(audioId));

        // Build a generate request based on the original + updated voice/format options
        GenerateAudioRequest regenerateRequest = new GenerateAudioRequest(
                original.getStoryId(),
                original.getChapterId(),
                original.getSectionId(),
                original.getLanguage(),
                request.voiceName(),
                request.voiceGender(),
                request.format()
        );
        return generateAudio(regenerateRequest);
    }

    @Override
    public void deleteAudio(String audioId) {
        AudioAssetDocument doc = repository.findById(audioId)
                .orElseThrow(() -> new AudioNotFoundException(audioId));
        audioStorage.delete(doc.getStoragePath());
        repository.deleteById(audioId);
    }

    @Override
    public byte[] getAudioContent(String audioId) {
        AudioAssetDocument doc = repository.findById(audioId)
                .orElseThrow(() -> new AudioNotFoundException(audioId));
        if (doc.getStatus() != AudioStatus.COMPLETED) {
            throw new AudioGenerationException(
                    "Audio is not yet available — current status: " + doc.getStatus());
        }
        return audioStorage.read(doc.getStoragePath());
    }

    private String computeContentHash(String content, AudioLanguage language,
                                       String voiceName, AudioFormat format) {
        String input = content + "|" + language + "|" + voiceName + "|" + format;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

}
