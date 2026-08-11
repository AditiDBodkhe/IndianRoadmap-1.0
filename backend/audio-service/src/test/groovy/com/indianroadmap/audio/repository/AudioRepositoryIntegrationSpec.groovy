package com.indianroadmap.audio.repository

import com.indianroadmap.audio.document.*
import org.junit.jupiter.api.Assumptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class AudioRepositoryIntegrationSpec extends Specification {

    @Shared
    static MongoDBContainer mongo

    static {
        try {
            mongo = new MongoDBContainer("mongo:8.0")
            mongo.start()
        } catch (Throwable ignored) {
            mongo = null
        }
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        if (mongo != null) {
            registry.add("spring.data.mongodb.uri", mongo::getConnectionString)
        }
    }

    @Autowired
    AudioAssetRepository repository

    def setup() {
        Assumptions.assumeTrue(mongo != null, "Docker unavailable — skipping integration test")
        repository.deleteAll()
    }

    def "save and findById"() {
        given:
        def doc = buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 1)

        when:
        def saved = repository.save(doc)
        def found = repository.findById(saved.id)

        then:
        found.isPresent()
        found.get().storyId == "story-1"
        found.get().sectionId == "section-1"
        found.get().language == AudioLanguage.ENGLISH
        found.get().version == 1
    }

    def "findByStoryId returns documents for that story"() {
        given:
        repository.save(buildDoc("story-A", "section-1", AudioLanguage.ENGLISH, 1))
        repository.save(buildDoc("story-A", "section-2", AudioLanguage.HINDI, 1))
        repository.save(buildDoc("story-B", "section-3", AudioLanguage.ENGLISH, 1))

        when:
        def results = repository.findByStoryId("story-A",
                org.springframework.data.domain.PageRequest.of(0, 10))

        then:
        results.totalElements == 2
        results.content.every { it.storyId == "story-A" }
    }

    def "findBySectionId returns documents for that section"() {
        given:
        repository.save(buildDoc("story-1", "section-X", AudioLanguage.ENGLISH, 1))
        repository.save(buildDoc("story-1", "section-X", AudioLanguage.HINDI, 1))
        repository.save(buildDoc("story-1", "section-Y", AudioLanguage.ENGLISH, 1))

        when:
        def results = repository.findBySectionId("section-X",
                org.springframework.data.domain.PageRequest.of(0, 10))

        then:
        results.totalElements == 2
    }

    def "findByStatus filters by status"() {
        given:
        def completed = buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 1)
        completed.status = AudioStatus.COMPLETED
        def failed = buildDoc("story-1", "section-2", AudioLanguage.ENGLISH, 1)
        failed.status = AudioStatus.FAILED
        repository.save(completed)
        repository.save(failed)

        when:
        def results = repository.findByStatus(AudioStatus.COMPLETED)

        then:
        results.size() == 1
        results[0].status == AudioStatus.COMPLETED
    }

    def "findBySectionIdAndLanguage filters correctly"() {
        given:
        repository.save(buildDoc("story-1", "section-A", AudioLanguage.ENGLISH, 1))
        repository.save(buildDoc("story-1", "section-A", AudioLanguage.HINDI, 1))

        when:
        def results = repository.findBySectionIdAndLanguage("section-A", AudioLanguage.HINDI,
                org.springframework.data.domain.PageRequest.of(0, 10))

        then:
        results.totalElements == 1
        results.content[0].language == AudioLanguage.HINDI
    }

    def "countByStoryIdAndSectionIdAndLanguage returns correct count"() {
        given:
        repository.save(buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 1))
        repository.save(buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 2))
        repository.save(buildDoc("story-1", "section-1", AudioLanguage.HINDI, 1))

        when:
        def count = repository.countByStoryIdAndSectionIdAndLanguage("story-1", "section-1", AudioLanguage.ENGLISH)

        then:
        count == 2
    }

    def "unique compound lookup"() {
        given:
        def doc = buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 1)
        doc.voiceName = "default"
        doc.format = AudioFormat.MP3
        repository.save(doc)

        when:
        def found = repository.findByStoryIdAndChapterIdAndSectionIdAndLanguageAndVoiceNameAndFormatAndVersion(
                "story-1", "chapter-1", "section-1", AudioLanguage.ENGLISH, "default", AudioFormat.MP3, 1)

        then:
        found.isPresent()
    }

    def "findByContentHash returns matching document"() {
        given:
        def doc = buildDoc("story-1", "section-1", AudioLanguage.ENGLISH, 1)
        doc.contentHash = "sha256-abc"
        repository.save(doc)

        when:
        def found = repository.findByContentHash("sha256-abc")

        then:
        found.isPresent()
        found.get().contentHash == "sha256-abc"
    }

    private static AudioAssetDocument buildDoc(String storyId, String sectionId, AudioLanguage lang, int version) {
        def doc = new AudioAssetDocument()
        doc.storyId = storyId
        doc.chapterId = "chapter-1"
        doc.sectionId = sectionId
        doc.language = lang
        doc.voiceName = "default"
        doc.voiceGender = VoiceGender.NEUTRAL
        doc.provider = TtsProviderType.MOCK
        doc.format = AudioFormat.MP3
        doc.status = AudioStatus.COMPLETED
        doc.version = version
        doc.createdAt = Instant.now()
        doc.updatedAt = Instant.now()
        doc
    }
}
