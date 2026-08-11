package com.indianroadmap.story

import com.indianroadmap.story.document.StoryLanguage
import com.indianroadmap.story.document.StoryStatus
import com.indianroadmap.story.document.StoryType
import spock.lang.Specification

class SanitySpec extends Specification {

    def "StoryStatus has expected values"() {
        expect:
        StoryStatus.values().length == 4
    }

    def "StoryType has expected values"() {
        expect:
        StoryType.values().length == 11
    }

    def "StoryLanguage has expected values"() {
        expect:
        StoryLanguage.values().length == 9
    }
}
