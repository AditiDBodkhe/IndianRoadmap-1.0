package com.indianroadmap.audio.mapper

import com.indianroadmap.audio.document.*
import com.indianroadmap.audio.dto.response.AudioResponse
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class AudioMapperSpec extends Specification {

    @Subject
    AudioMapper mapper = new AudioMapper()

    def "toResponse maps all fields correctly"() {
        given:
        Instant now = Instant.parse("2026-01-01T00:00:00Z")
        def doc = new AudioAssetDocument()
        doc.id = "audio-1"
        doc.storyId = "story-1"
        doc.chapterId = "chapter-1"
        doc.sectionId = "section-1"
        doc.language = AudioLanguage.ENGLISH
        doc.voiceName = "default"
        doc.voiceGender = VoiceGender.NEUTRAL
        doc.provider = TtsProviderType.MOCK
        doc.format = AudioFormat.MP3
        doc.status = AudioStatus.COMPLETED
        doc.storagePath = "story-1/section-1/english/audio-v1.mp3"
        doc.publicUrl = null
        doc.durationSeconds = 15.5
        doc.fileSizeBytes = 8192L
        doc.contentHash = "abc123"
        doc.errorMessage = null
        doc.version = 1
        doc.createdAt = now
        doc.updatedAt = now
        doc.completedAt = now

        when:
        AudioResponse response = mapper.toResponse(doc)

        then:
        response.id() == "audio-1"
        response.storyId() == "story-1"
        response.chapterId() == "chapter-1"
        response.sectionId() == "section-1"
        response.language() == AudioLanguage.ENGLISH
        response.voiceName() == "default"
        response.voiceGender() == VoiceGender.NEUTRAL
        response.provider() == TtsProviderType.MOCK
        response.format() == AudioFormat.MP3
        response.status() == AudioStatus.COMPLETED
        response.storagePath() == "story-1/section-1/english/audio-v1.mp3"
        response.durationSeconds() == 15.5
        response.fileSizeBytes() == 8192L
        response.contentHash() == "abc123"
        response.version() == 1
        response.createdAt() == now
        response.updatedAt() == now
        response.completedAt() == now
    }

    def "toResponse handles null optional fields"() {
        given:
        def doc = new AudioAssetDocument()
        doc.id = "audio-2"
        doc.storyId = "story-2"
        doc.sectionId = "section-2"
        doc.language = AudioLanguage.HINDI
        doc.voiceName = "default"
        doc.voiceGender = VoiceGender.FEMALE
        doc.format = AudioFormat.WAV
        doc.status = AudioStatus.FAILED
        doc.version = 1
        doc.errorMessage = "TTS failed"
        doc.createdAt = Instant.now()
        doc.updatedAt = Instant.now()

        when:
        AudioResponse response = mapper.toResponse(doc)

        then:
        response.storagePath() == null
        response.durationSeconds() == null
        response.fileSizeBytes() == null
        response.completedAt() == null
        response.errorMessage() == "TTS failed"
    }
}
