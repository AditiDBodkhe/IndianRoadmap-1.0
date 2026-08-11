package com.indianroadmap.story.service

import com.indianroadmap.story.calculator.ReadingTimeCalculator
import com.indianroadmap.story.client.DestinationClient
import com.indianroadmap.story.client.DestinationSummary
import com.indianroadmap.story.document.*
import com.indianroadmap.story.dto.request.*
import com.indianroadmap.story.dto.response.StoryResponse
import com.indianroadmap.story.exception.*
import com.indianroadmap.story.mapper.StoryMapper
import com.indianroadmap.story.repository.StoryRepository
import com.indianroadmap.story.validation.StoryStructureValidator
import com.indianroadmap.story.validation.StoryValidator
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class StoryServiceSpec extends Specification {

    def repository = Mock(StoryRepository)
    def mapper = Mock(StoryMapper)
    def destinationClient = Mock(DestinationClient)
    def validator = Mock(StoryValidator)
    def structureValidator = Mock(StoryStructureValidator)
    def readingTimeCalculator = Mock(ReadingTimeCalculator)
    def mongoTemplate = Mock(MongoTemplate)
    def clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Subject
    StoryServiceImpl service = new StoryServiceImpl(
            repository, mapper, destinationClient, validator, structureValidator, readingTimeCalculator, clock, mongoTemplate)

    def "createStory success"() {
        given:
        def request = new CreateStoryRequest("Spiti Valley", "dest-1", "Spiti Valley", "A cultural story",
                StoryType.CULTURE, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH])
        def document = new StoryDocument()
        document.setId("story-1")
        document.setSlug("spiti-valley")
        document.setDestinationId("dest-1")
        document.setTitle("Spiti Valley")
        document.setStoryType(StoryType.CULTURE)
        document.setDifficulty(StoryDifficulty.STANDARD)
        document.setAvailableLanguages([StoryLanguage.ENGLISH])
        document.setCreatedAt(Instant.now(clock))
        document.setUpdatedAt(Instant.now(clock))
        def response = responseFor(document)

        when:
        validator.normalizeSlug("Spiti Valley") >> "spiti-valley"
        repository.existsBySlug("spiti-valley") >> false
        destinationClient.getDestination("dest-1") >> new DestinationSummary("dest-1", "dest", "Dest", "HP", "Spiti")
        mapper.mapToDocument(_ as CreateStoryRequest, clock) >> document
        readingTimeCalculator.calculate(document) >> 1
        repository.save(document) >> document
        mapper.mapToResponse(document) >> response
        def result = service.createStory(request)

        then:
        result.id() == "story-1"
    }

    def "duplicate slug throws DuplicateStoryException"() {
        given:
        def request = new CreateStoryRequest("existing", "dest-1", "Title", null,
                StoryType.HISTORY, StoryDifficulty.SIMPLE, [StoryLanguage.ENGLISH])

        when:
        validator.normalizeSlug("existing") >> "existing"
        repository.existsBySlug("existing") >> true
        service.createStory(request)

        then:
        thrown(DuplicateStoryException)
    }

    def "missing destination throws DestinationNotFoundException"() {
        given:
        def request = new CreateStoryRequest("story", "missing", "Title", null,
                StoryType.HISTORY, StoryDifficulty.SIMPLE, [StoryLanguage.ENGLISH])
        def document = new StoryDocument()

        when:
        validator.normalizeSlug("story") >> "story"
        repository.existsBySlug("story") >> false
        destinationClient.getDestination("missing") >> { throw new DestinationNotFoundException("missing") }
        mapper.mapToDocument(_ as CreateStoryRequest, clock) >> document
        service.createStory(request)

        then:
        thrown(DestinationNotFoundException)
    }

    def "getStory success"() {
        given:
        def story = new StoryDocument()
        story.setId("story-1")
        def response = responseFor(story)

        when:
        repository.findById("story-1") >> Optional.of(story)
        mapper.mapToResponse(story) >> response
        def result = service.getStory("story-1")

        then:
        result.id() == "story-1"
    }

    def "getStory missing throws StoryNotFoundException"() {
        when:
        repository.findById("missing") >> Optional.empty()
        service.getStory("missing")

        then:
        thrown(StoryNotFoundException)
    }

    def "addChapter adds chapter and resequences"() {
        given:
        def story = basicStory("story-1")
        def request = new AddChapterRequest("Origins", 1)
        def saved = basicStory("story-1")
        saved.setChapters(story.getChapters())
        def response = responseFor(saved)

        when:
        repository.findById("story-1") >> Optional.of(story)
        readingTimeCalculator.calculate(_ as StoryDocument) >> 1
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> response
        def result = service.addChapter("story-1", request)

        then:
        result != null
        story.getChapters().size() == 1
        story.getChapters()[0].title == "Origins"
    }

    def "deleteChapter removes chapter"() {
        given:
        def chapter = new StoryChapterDocument("chapter-1", 1, "Origins", [])
        def story = basicStory("story-1")
        story.setChapters(new ArrayList<>([chapter]))
        when:
        repository.findById("story-1") >> Optional.of(story)
        readingTimeCalculator.calculate(_ as StoryDocument) >> 1
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.deleteChapter("story-1", "chapter-1")

        then:
        result != null
        story.getChapters().isEmpty()
    }

    def "addSection adds section to chapter"() {
        given:
        def chapter = new StoryChapterDocument("chapter-1", 1, "Origins", [])
        def story = basicStory("story-1")
        story.setChapters(new ArrayList<>([chapter]))
        def request = new AddSectionRequest("Heading", "Some content here", 1, StoryLanguage.ENGLISH)

        when:
        repository.findById("story-1") >> Optional.of(story)
        readingTimeCalculator.calculate(_ as StoryDocument) >> 2
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.addSection("story-1", "chapter-1", request)

        then:
        result != null
        story.getChapters()[0].getSections().size() == 1
        story.getChapters()[0].getSections()[0].language == StoryLanguage.ENGLISH
    }

    def "deleteSection removes section from chapter"() {
        given:
        def section = new StorySectionDocument("section-1", 1, "Heading", "Content", StoryLanguage.ENGLISH)
        def chapter = new StoryChapterDocument("chapter-1", 1, "Origins", new ArrayList<>([section]))
        def story = basicStory("story-1")
        story.setChapters(new ArrayList<>([chapter]))

        when:
        repository.findById("story-1") >> Optional.of(story)
        readingTimeCalculator.calculate(_ as StoryDocument) >> 1
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.deleteSection("story-1", "chapter-1", "section-1")

        then:
        result != null
        story.getChapters()[0].getSections().isEmpty()
    }

    def "submitForReview updates status"() {
        given:
        def story = storyWithStructure("story-1", StoryStatus.DRAFT)

        when:
        repository.findById("story-1") >> Optional.of(story)
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.submitForReview("story-1")

        then:
        result.status() == StoryStatus.REVIEW
    }

    def "publishStory from REVIEW sets published status"() {
        given:
        def story = storyWithStructure("story-1", StoryStatus.REVIEW)

        when:
        repository.findById("story-1") >> Optional.of(story)
        readingTimeCalculator.calculate(_ as StoryDocument) >> 1
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.publishStory("story-1")

        then:
        result.status() == StoryStatus.PUBLISHED
        result.publishedAt() == Instant.parse("2026-01-01T00:00:00Z")
    }

    def "archiveStory updates status"() {
        given:
        def story = storyWithStructure("story-1", StoryStatus.PUBLISHED)

        when:
        repository.findById("story-1") >> Optional.of(story)
        repository.save(_ as StoryDocument) >> { StoryDocument doc -> doc }
        mapper.mapToResponse(_ as StoryDocument) >> { StoryDocument doc -> responseFor(doc) }
        def result = service.archiveStory("story-1")

        then:
        result.status() == StoryStatus.ARCHIVED
    }

    def "invalid status transition bubbles up"() {
        given:
        def story = storyWithStructure("story-1", StoryStatus.ARCHIVED)

        when:
        repository.findById("story-1") >> Optional.of(story)
        validator.validateStatusTransition(StoryStatus.ARCHIVED, StoryStatus.REVIEW) >> { throw new InvalidStoryStatusException("bad") }
        service.submitForReview("story-1")

        then:
        thrown(InvalidStoryStatusException)
    }

    @Unroll
    def "status transition from #from to #to is valid"() {
        given:
        def localValidator = new StoryValidator()

        when:
        localValidator.validateStatusTransition(from, to)

        then:
        noExceptionThrown()

        where:
        from                   | to
        StoryStatus.DRAFT      | StoryStatus.REVIEW
        StoryStatus.REVIEW     | StoryStatus.PUBLISHED
        StoryStatus.DRAFT      | StoryStatus.ARCHIVED
        StoryStatus.REVIEW     | StoryStatus.ARCHIVED
        StoryStatus.PUBLISHED  | StoryStatus.ARCHIVED
    }

    @Unroll
    def "status transition from #from to #to is rejected"() {
        given:
        def localValidator = new StoryValidator()

        when:
        localValidator.validateStatusTransition(from, to)

        then:
        thrown(InvalidStoryStatusException)

        where:
        from                 | to
        StoryStatus.ARCHIVED | StoryStatus.PUBLISHED
        StoryStatus.ARCHIVED | StoryStatus.REVIEW
    }

    private static StoryDocument basicStory(String id) {
        def story = new StoryDocument()
        story.setId(id)
        story.setSlug("story")
        story.setDestinationId("dest-1")
        story.setTitle("Story")
        story.setStoryType(StoryType.CULTURE)
        story.setDifficulty(StoryDifficulty.STANDARD)
        story.setStatus(StoryStatus.DRAFT)
        story.setAvailableLanguages([StoryLanguage.ENGLISH])
        story.setChapters(new ArrayList<>())
        story.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"))
        story.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"))
        story
    }

    private static StoryDocument storyWithStructure(String id, StoryStatus status) {
        def section = new StorySectionDocument("section-1", 1, "Heading", "Content words", StoryLanguage.ENGLISH)
        def chapter = new StoryChapterDocument("chapter-1", 1, "Origins", new ArrayList<>([section]))
        def story = basicStory(id)
        story.setStatus(status)
        story.setChapters(new ArrayList<>([chapter]))
        story
    }

    private static StoryResponse responseFor(StoryDocument story) {
        new StoryResponse(
                story.getId(),
                story.getSlug(),
                story.getDestinationId(),
                story.getTitle(),
                story.getShortDescription(),
                story.getStoryType(),
                story.getStatus(),
                story.getDifficulty(),
                story.getAvailableLanguages(),
                [],
                story.getEstimatedReadingTimeMinutes(),
                story.getCreatedAt(),
                story.getUpdatedAt(),
                story.getPublishedAt())
    }
}
