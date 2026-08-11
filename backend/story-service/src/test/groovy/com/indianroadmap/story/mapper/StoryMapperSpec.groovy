package com.indianroadmap.story.mapper

import com.indianroadmap.story.document.*
import com.indianroadmap.story.dto.request.CreateStoryRequest
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StoryMapperSpec extends Specification {

    def mapper = new StoryMapper()
    def clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    def "mapToDocument maps all fields correctly"() {
        given:
        def request = new CreateStoryRequest("Spiti Valley", "dest-1", "Spiti Valley", "Story summary",
                StoryType.CULTURE, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH, StoryLanguage.HINDI])

        when:
        def document = mapper.mapToDocument(request, clock)

        then:
        document.slug == "spiti-valley"
        document.destinationId == "dest-1"
        document.title == "Spiti Valley"
        document.shortDescription == "Story summary"
        document.storyType == StoryType.CULTURE
        document.status == StoryStatus.DRAFT
        document.difficulty == StoryDifficulty.STANDARD
        document.availableLanguages == [StoryLanguage.ENGLISH, StoryLanguage.HINDI]
        document.createdAt == Instant.parse("2026-01-01T00:00:00Z")
    }

    def "mapToResponse includes chapters and sections"() {
        given:
        def section = new StorySectionDocument("section-1", 1, "Heading", "Content", StoryLanguage.ENGLISH)
        def chapter = new StoryChapterDocument("chapter-1", 1, "Origins", [section])
        def document = new StoryDocument()
        document.setId("story-1")
        document.setSlug("slug")
        document.setDestinationId("dest-1")
        document.setTitle("Title")
        document.setStoryType(StoryType.HISTORY)
        document.setStatus(StoryStatus.REVIEW)
        document.setDifficulty(StoryDifficulty.DETAILED)
        document.setAvailableLanguages([StoryLanguage.ENGLISH])
        document.setChapters([chapter])
        document.setEstimatedReadingTimeMinutes(3)
        document.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"))
        document.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"))

        when:
        def response = mapper.mapToResponse(document)

        then:
        response.id() == "story-1"
        response.chapters().size() == 1
        response.chapters()[0].chapterId() == "chapter-1"
        response.chapters()[0].sections()[0].sectionId() == "section-1"
        response.estimatedReadingTimeMinutes() == 3
    }

    def "mapToSummaryResponse excludes chapters"() {
        given:
        def document = new StoryDocument()
        document.setId("story-1")
        document.setSlug("slug")
        document.setDestinationId("dest-1")
        document.setTitle("Title")
        document.setShortDescription("Summary")
        document.setStoryType(StoryType.TRAVEL)
        document.setStatus(StoryStatus.DRAFT)
        document.setDifficulty(StoryDifficulty.SIMPLE)
        document.setAvailableLanguages([StoryLanguage.ENGLISH])
        document.setEstimatedReadingTimeMinutes(2)
        document.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"))
        document.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"))

        when:
        def summary = mapper.mapToSummaryResponse(document)

        then:
        summary.id() == "story-1"
        summary.title() == "Title"
        summary.estimatedReadingTimeMinutes() == 2
        summary.metaClass.respondsTo(summary, 'getChapters').isEmpty()
    }
}
