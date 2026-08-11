package com.indianroadmap.audio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.indianroadmap.audio.document.*
import com.indianroadmap.audio.dto.request.GenerateAudioRequest
import com.indianroadmap.audio.dto.request.RegenerateAudioRequest
import com.indianroadmap.audio.dto.response.AudioResponse
import com.indianroadmap.audio.dto.response.PageResponse
import com.indianroadmap.audio.exception.AudioNotFoundException
import com.indianroadmap.audio.service.AudioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [AudioController])
class AudioControllerSpec extends Specification {

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    AudioService audioService

    private static AudioResponse sampleAudioResponse() {
        new AudioResponse("audio-1", "story-1", "chapter-1", "section-1",
                AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, TtsProviderType.MOCK,
                AudioFormat.MP3, AudioStatus.COMPLETED, "story-1/section-1/english/audio-v1.mp3",
                null, 5.0, 512L, "hash123", null, 1,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"))
    }

    def "POST /api/v1/audio returns 201"() {
        given:
        when(audioService.generateAudio(any())).thenReturn(sampleAudioResponse())
        def body = MAPPER.writeValueAsString(new GenerateAudioRequest(
                "story-1", "chapter-1", "section-1",
                AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, AudioFormat.MP3))

        expect:
        mockMvc.perform(post("/api/v1/audio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.id').value('audio-1'))
    }

    def "POST /api/v1/audio returns 400 for missing required fields"() {
        given:
        def body = '{"storyId": "", "chapterId": "c1", "sectionId": "s1"}'

        expect:
        mockMvc.perform(post("/api/v1/audio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
    }

    def "GET /api/v1/audio/{id} returns 200 for existing audio"() {
        given:
        when(audioService.getAudio(eq("audio-1"))).thenReturn(sampleAudioResponse())

        expect:
        mockMvc.perform(get("/api/v1/audio/audio-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value('audio-1'))
                .andExpect(jsonPath('$.data.status').value('COMPLETED'))
    }

    def "GET /api/v1/audio/{id} returns 404 for missing audio"() {
        given:
        when(audioService.getAudio(eq("missing"))).thenThrow(new AudioNotFoundException("missing"))

        expect:
        mockMvc.perform(get("/api/v1/audio/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.error.code').value('AUDIO_NOT_FOUND'))
    }

    def "DELETE /api/v1/audio/{id} returns 204"() {
        expect:
        mockMvc.perform(delete("/api/v1/audio/audio-1"))
                .andExpect(status().isNoContent())
    }

    def "POST /api/v1/audio/{id}/regenerate returns 201"() {
        given:
        when(audioService.regenerateAudio(eq("audio-1"), any())).thenReturn(sampleAudioResponse())
        def body = MAPPER.writeValueAsString(new RegenerateAudioRequest("default", VoiceGender.NEUTRAL, AudioFormat.MP3))

        expect:
        mockMvc.perform(post("/api/v1/audio/audio-1/regenerate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "GET /api/v1/audio/story/{storyId} returns 200"() {
        given:
        def pageResponse = PageResponse.of([sampleAudioResponse()], 0, 20, 1L)
        when(audioService.listByStory(eq("story-1"), isNull(), isNull(), any())).thenReturn(pageResponse)

        expect:
        mockMvc.perform(get("/api/v1/audio/story/story-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].storyId').value('story-1'))
    }

    def "GET /api/v1/audio/section/{sectionId} returns 200"() {
        given:
        def pageResponse = PageResponse.of([sampleAudioResponse()], 0, 20, 1L)
        when(audioService.listBySection(eq("section-1"), isNull(), isNull(), any())).thenReturn(pageResponse)

        expect:
        mockMvc.perform(get("/api/v1/audio/section/section-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].sectionId').value('section-1'))
    }

    def "GET /api/v1/audio/{id}/content returns audio bytes"() {
        given:
        when(audioService.getAudio(eq("audio-1"))).thenReturn(sampleAudioResponse())
        when(audioService.getAudioContent(eq("audio-1"))).thenReturn("mock audio bytes".bytes)

        expect:
        mockMvc.perform(get("/api/v1/audio/audio-1/content"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
    }

    def "content type for audio formats"() {
        given:
        def responseForFormat = new AudioResponse("audio-1", "story-1", "chapter-1", "section-1",
                AudioLanguage.ENGLISH, "default", VoiceGender.NEUTRAL, TtsProviderType.MOCK,
                format, AudioStatus.COMPLETED, "path", null, 5.0, 512L, "hash", null, 1,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"))
        when(audioService.getAudio(eq("audio-1"))).thenReturn(responseForFormat)
        when(audioService.getAudioContent(eq("audio-1"))).thenReturn("bytes".bytes)

        expect:
        mockMvc.perform(get("/api/v1/audio/audio-1/content"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString(expectedContentType)))

        where:
        format          | expectedContentType
        AudioFormat.MP3 | "audio/mpeg"
        AudioFormat.WAV | "audio/wav"
        AudioFormat.OGG | "audio/ogg"
    }
}
