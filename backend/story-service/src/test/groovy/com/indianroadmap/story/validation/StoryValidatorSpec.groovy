package com.indianroadmap.story.validation

import com.indianroadmap.story.document.StoryDifficulty
import com.indianroadmap.story.document.StoryStatus
import com.indianroadmap.story.document.StoryType
import com.indianroadmap.story.dto.request.CreateStoryRequest
import com.indianroadmap.story.exception.InvalidStoryException
import com.indianroadmap.story.exception.InvalidStoryStatusException
import spock.lang.Specification

class StoryValidatorSpec extends Specification {

    def validator = new StoryValidator()

    def "normalizeSlug converts spaces to hyphen"() {
        expect:
        validator.normalizeSlug("Spiti Valley") == "spiti-valley"
    }

    def "validateCreateRequest rejects blank slug"() {
        when:
        validator.validateCreateRequest(new CreateStoryRequest("", "dest-1", "Title", null,
                StoryType.HISTORY, StoryDifficulty.SIMPLE, []))

        then:
        thrown(InvalidStoryException)
    }

    def "validateCreateRequest rejects blank title"() {
        when:
        validator.validateCreateRequest(new CreateStoryRequest("slug", "dest-1", "", null,
                StoryType.HISTORY, StoryDifficulty.SIMPLE, []))

        then:
        thrown(InvalidStoryException)
    }

    def "validateCreateRequest rejects null storyType"() {
        when:
        validator.validateCreateRequest(new CreateStoryRequest("slug", "dest-1", "Title", null,
                null, StoryDifficulty.SIMPLE, []))

        then:
        thrown(InvalidStoryException)
    }

    def "validateStatusTransition accepts valid transitions"() {
        expect:
        validator.validateStatusTransition(StoryStatus.DRAFT, StoryStatus.REVIEW)
        validator.validateStatusTransition(StoryStatus.REVIEW, StoryStatus.PUBLISHED)
        validator.validateStatusTransition(StoryStatus.PUBLISHED, StoryStatus.ARCHIVED)
    }

    def "validateStatusTransition rejects archived to published"() {
        when:
        validator.validateStatusTransition(StoryStatus.ARCHIVED, StoryStatus.PUBLISHED)

        then:
        thrown(InvalidStoryStatusException)
    }
}
