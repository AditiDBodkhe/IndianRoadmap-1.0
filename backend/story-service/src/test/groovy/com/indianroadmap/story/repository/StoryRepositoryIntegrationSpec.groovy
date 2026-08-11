package com.indianroadmap.story.repository

import com.indianroadmap.story.document.StoryDifficulty
import com.indianroadmap.story.document.StoryDocument
import com.indianroadmap.story.document.StoryLanguage
import com.indianroadmap.story.document.StoryStatus
import com.indianroadmap.story.document.StoryType
import org.junit.jupiter.api.Assumptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class StoryRepositoryIntegrationSpec extends Specification {

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
    static void mongoProps(DynamicPropertyRegistry registry) {
        if (mongo != null) {
            registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl)
        }
    }

    @Autowired
    StoryRepository repository

    def setupSpec() {
        Assumptions.assumeTrue(mongo != null, "Docker unavailable")
    }

    def cleanup() {
        repository.deleteAll()
    }

    def cleanupSpec() {
        if (mongo != null) {
            mongo.stop()
        }
    }

    def "save and find by slug"() {
        given:
        repository.save(buildStory("spiti-story", "Spiti Story", StoryStatus.DRAFT, "dest-1"))

        when:
        def result = repository.findBySlug("spiti-story")

        then:
        result.present
        result.get().title == "Spiti Story"
    }

    def "find by destination id"() {
        given:
        repository.save(buildStory("story-1", "Story 1", StoryStatus.DRAFT, "dest-1"))
        repository.save(buildStory("story-2", "Story 2", StoryStatus.PUBLISHED, "dest-1"))

        expect:
        repository.findByDestinationId("dest-1").size() == 2
    }

    def "find by status with pagination"() {
        given:
        repository.save(buildStory("story-1", "Story 1", StoryStatus.DRAFT, "dest-1"))
        repository.save(buildStory("story-2", "Story 2", StoryStatus.PUBLISHED, "dest-1"))

        when:
        def page = repository.findByStatus(StoryStatus.DRAFT, PageRequest.of(0, 10))

        then:
        page.totalElements == 1
        page.content[0].slug == "story-1"
    }

    def "unique slug constraint is enforced"() {
        given:
        repository.save(buildStory("unique-story", "Story 1", StoryStatus.DRAFT, "dest-1"))

        when:
        repository.save(buildStory("unique-story", "Story 2", StoryStatus.DRAFT, "dest-2"))

        then:
        thrown(Exception)
    }

    private static StoryDocument buildStory(String slug, String title, StoryStatus status, String destinationId) {
        def story = new StoryDocument()
        story.setSlug(slug)
        story.setDestinationId(destinationId)
        story.setTitle(title)
        story.setShortDescription("Summary")
        story.setStoryType(StoryType.HISTORY)
        story.setStatus(status)
        story.setDifficulty(StoryDifficulty.STANDARD)
        story.setAvailableLanguages([StoryLanguage.ENGLISH])
        story.setEstimatedReadingTimeMinutes(1)
        story.setCreatedAt(Instant.now())
        story.setUpdatedAt(Instant.now())
        story
    }
}
