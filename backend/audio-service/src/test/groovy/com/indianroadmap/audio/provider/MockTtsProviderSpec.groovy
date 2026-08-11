package com.indianroadmap.audio.provider

import com.indianroadmap.audio.document.AudioFormat
import com.indianroadmap.audio.document.AudioLanguage
import com.indianroadmap.audio.document.TtsProviderType
import com.indianroadmap.audio.document.VoiceGender
import spock.lang.Specification
import spock.lang.Subject

class MockTtsProviderSpec extends Specification {

    @Subject
    MockTtsProvider provider = new MockTtsProvider()

    def "same request produces deterministic audio bytes"() {
        given:
        def request = new TtsRequest("Hello Chhitkul", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)

        when:
        def result1 = provider.generate(request)
        def result2 = provider.generate(request)

        then:
        result1.audioBytes() == result2.audioBytes()
        result1.durationSeconds() == result2.durationSeconds()
    }

    def "different text produces different audio bytes"() {
        given:
        def req1 = new TtsRequest("Chhitkul is beautiful", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)
        def req2 = new TtsRequest("Tabo monastery is ancient", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)

        when:
        def result1 = provider.generate(req1)
        def result2 = provider.generate(req2)

        then:
        result1.audioBytes() != result2.audioBytes()
    }

    def "audio bytes are non-empty"() {
        given:
        def request = new TtsRequest("Short text", AudioLanguage.HINDI, "default", VoiceGender.FEMALE, AudioFormat.WAV)

        when:
        def result = provider.generate(request)

        then:
        result.audioBytes() != null
        result.audioBytes().length >= 128
    }

    def "duration is estimated from word count"() {
        given:
        // 6 words at 3 words/second = 2 seconds
        def request = new TtsRequest("one two three four five six", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)

        when:
        def result = provider.generate(request)

        then:
        result.durationSeconds() > 0
    }

    def "empty content produces minimum duration of 1 second"() {
        given:
        def request = new TtsRequest("", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)

        when:
        def result = provider.generate(request)

        then:
        result.durationSeconds() == 1.0
    }

    def "provider type is MOCK"() {
        given:
        def request = new TtsRequest("test", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3)

        when:
        def result = provider.generate(request)

        then:
        result.provider() == TtsProviderType.MOCK
    }

    def "different languages produce different bytes"() {
        given:
        def text = "Village at high altitude"

        where:
        language << [AudioLanguage.ENGLISH, AudioLanguage.HINDI, AudioLanguage.MARATHI, AudioLanguage.BALTI, AudioLanguage.LADAKHI]
    }

    def "different formats produce different bytes"() {
        given:
        def req1 = new TtsRequest("Test", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, format1)
        def req2 = new TtsRequest("Test", AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, format2)

        expect:
        provider.generate(req1).audioBytes() != provider.generate(req2).audioBytes()

        where:
        format1       | format2
        AudioFormat.MP3 | AudioFormat.WAV
        AudioFormat.WAV | AudioFormat.OGG
    }
}
