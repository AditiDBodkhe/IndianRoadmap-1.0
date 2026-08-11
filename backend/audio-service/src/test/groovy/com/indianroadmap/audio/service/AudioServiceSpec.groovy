package com.indianroadmap.audio.service

import com.indianroadmap.audio.client.StoryClient
import com.indianroadmap.audio.client.StorySectionSummary
import com.indianroadmap.audio.document.*
import com.indianroadmap.audio.dto.request.GenerateAudioRequest
import com.indianroadmap.audio.dto.request.RegenerateAudioRequest
import com.indianroadmap.audio.dto.response.AudioResponse
import com.indianroadmap.audio.exception.AudioAlreadyExistsException
import com.indianroadmap.audio.exception.AudioGenerationException
import com.indianroadmap.audio.exception.AudioNotFoundException
import com.indianroadmap.audio.exception.StoryNotFoundException
import com.indianroadmap.audio.exception.UnsupportedLanguageException
import com.indianroadmap.audio.mapper.AudioMapper
import com.indianroadmap.audio.provider.MockTtsProvider
import com.indianroadmap.audio.provider.TtsProvider
import com.indianroadmap.audio.provider.TtsProviderFactory
import com.indianroadmap.audio.provider.TtsRequest
import com.indianroadmap.audio.provider.TtsResult
import com.indianroadmap.audio.repository.AudioAssetRepository
import com.indianroadmap.audio.storage.AudioStorage
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

class AudioServiceSpec extends Specification {

    AudioAssetRepository repository = Mock()
    StoryClient storyClient = Mock()
    AudioMapper mapper = new AudioMapper()
    TtsProvider ttsProvider = Mock()
    TtsProviderFactory ttsProviderFactory = Mock()
    AudioStorage audioStorage = Mock()
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Subject
    AudioServiceImpl service = new AudioServiceImpl(repository, storyClient, mapper, ttsProviderFactory, audioStorage, clock)

    private static GenerateAudioRequest sampleRequest() {
        new GenerateAudioRequest("story-1", "chapter-1", "section-1",
                AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)
    }

    private static StorySectionSummary sampleSection() {
        new StorySectionSummary("section-1", 1, "The Village", "Chhitkul is the last village", AudioLanguage.ENGLISH)
    }

    private static AudioAssetDocument savedDoc(String id) {
        def doc = new AudioAssetDocument()
        doc.id = id
        doc.storyId = "story-1"
        doc.chapterId = "chapter-1"
        doc.sectionId = "section-1"
        doc.language = AudioLanguage.ENGLISH
        doc.voiceName = "default"
        doc.voiceGender = VoiceGender.NEUTRAL
        doc.format = AudioFormat.MP3
        doc.provider = TtsProviderType.MOCK
        doc.status = AudioStatus.COMPLETED
        doc.storagePath = "story-1/section-1/english/audio-v1.mp3"
        doc.durationSeconds = 5.0
        doc.fileSizeBytes = 512L
        doc.version = 1
        doc.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        doc.updatedAt = Instant.parse("2026-01-01T00:00:00Z")
        doc.completedAt = Instant.parse("2026-01-01T00:00:00Z")
        doc
    }

    def "generateAudio calls story-service, TTS provider, and storage"() {
        given:
        repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(_, _, _, _, _, _) >> []
        ttsProviderFactory.get() >> ttsProvider
        audioStorage.store(_, _, _, _, _, _, _) >> "story-1/section-1/english/audio-v1.mp3"
        repository.save(_) >> { AudioAssetDocument d -> savedDoc("audio-new") }

        when:
        AudioResponse response = service.generateAudio(sampleRequest())

        then:
        1 * storyClient.getSection("story-1", "chapter-1", "section-1") >> sampleSection()
        1 * ttsProvider.generate(_) >> new TtsResult("mock".bytes, 2.0, TtsProviderType.MOCK)
        1 * audioStorage.store(_, _, _, _, _, _, _) >> "story-1/section-1/english/audio-v1.mp3"
        response != null
    }

    def "generateAudio throws StoryNotFoundException when section not found"() {
        given:
        storyClient.getSection(_, _, _) >> { throw new StoryNotFoundException("not found") }

        when:
        service.generateAudio(sampleRequest())

        then:
        thrown(StoryNotFoundException)
    }

    def "generateAudio throws UnsupportedLanguageException when language does not match"() {
        given:
        def hindiSection = new StorySectionSummary("section-1", 1, "गाँव", "छितकुल एक गाँव है", AudioLanguage.HINDI)
        storyClient.getSection(_, _, _) >> hindiSection
        repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(_, _, _, _, _, _) >> []

        when:
        service.generateAudio(sampleRequest()) // requests ENGLISH but section is HINDI

        then:
        thrown(UnsupportedLanguageException)
    }

    def "generateAudio throws AudioGenerationException for empty section content"() {
        given:
        def emptySection = new StorySectionSummary("section-1", 1, "Heading", "", AudioLanguage.ENGLISH)
        storyClient.getSection(_, _, _) >> emptySection
        repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(_, _, _, _, _, _) >> []

        when:
        service.generateAudio(sampleRequest())

        then:
        thrown(AudioGenerationException)
    }

    def "generateAudio throws AudioAlreadyExistsException when completed audio exists"() {
        given:
        def existing = savedDoc("existing-audio")
        repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(_, _, _, _, _, _) >> [existing]
        storyClient.getSection(_, _, _) >> sampleSection()

        when:
        service.generateAudio(sampleRequest())

        then:
        thrown(AudioAlreadyExistsException)
    }

    def "getAudio returns AudioResponse for existing ID"() {
        given:
        repository.findById("audio-1") >> Optional.of(savedDoc("audio-1"))

        when:
        AudioResponse response = service.getAudio("audio-1")

        then:
        response.id() == "audio-1"
    }

    def "getAudio throws AudioNotFoundException for unknown ID"() {
        given:
        repository.findById("missing") >> Optional.empty()

        when:
        service.getAudio("missing")

        then:
        thrown(AudioNotFoundException)
    }

    def "deleteAudio removes file and document"() {
        given:
        def doc = savedDoc("audio-del")
        repository.findById("audio-del") >> Optional.of(doc)

        when:
        service.deleteAudio("audio-del")

        then:
        1 * audioStorage.delete("story-1/section-1/english/audio-v1.mp3")
        1 * repository.deleteById("audio-del")
    }

    def "deleteAudio throws AudioNotFoundException for unknown ID"() {
        given:
        repository.findById("missing") >> Optional.empty()

        when:
        service.deleteAudio("missing")

        then:
        thrown(AudioNotFoundException)
    }

    def "regenerateAudio creates new version"() {
        given:
        def original = savedDoc("audio-orig")
        repository.findById("audio-orig") >> Optional.of(original)
        storyClient.getSection(_, _, _) >> sampleSection()
        repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormat(_, _, _, _, _, _) >> []
        ttsProviderFactory.get() >> ttsProvider
        ttsProvider.generate(_) >> new TtsResult("mock-v2".bytes, 2.5, TtsProviderType.MOCK)
        audioStorage.store(_, _, _, _, _, _, _) >> "story-1/section-1/english/audio-v2.mp3"
        repository.save(_) >> { AudioAssetDocument d ->
            d.id = "audio-new"
            d
        }

        when:
        AudioResponse response = service.regenerateAudio("audio-orig",
                new RegenerateAudioRequest("female-voice", VoiceGender.FEMALE, AudioFormat.MP3))

        then:
        response != null
    }

    def "getAudioContent throws AudioGenerationException for non-completed audio"() {
        given:
        def doc = new AudioAssetDocument()
        doc.id = "audio-pending"
        doc.status = AudioStatus.GENERATING
        repository.findById("audio-pending") >> Optional.of(doc)

        when:
        service.getAudioContent("audio-pending")

        then:
        thrown(AudioGenerationException)
    }

    def "listByStory returns paged results"() {
        given:
        def doc = savedDoc("audio-1")
        repository.findByStoryId("story-1", _) >> new PageImpl<>([doc])

        when:
        def result = service.listByStory("story-1", null, null, PageRequest.of(0, 10))

        then:
        result.data().size() == 1
        result.meta().totalElements() == 1
    }
}
