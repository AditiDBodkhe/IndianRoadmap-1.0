package com.indianroadmap.story.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.indianroadmap.story.document.StoryDifficulty
import com.indianroadmap.story.document.StoryLanguage
import com.indianroadmap.story.document.StoryStatus
import com.indianroadmap.story.document.StoryType
import com.indianroadmap.story.dto.request.AddChapterRequest
import com.indianroadmap.story.dto.request.AddSectionRequest
import com.indianroadmap.story.dto.request.CreateStoryRequest
import com.indianroadmap.story.dto.request.UpdateStoryRequest
import com.indianroadmap.story.dto.response.StoryChapterResponse
import com.indianroadmap.story.dto.response.StoryResponse
import com.indianroadmap.story.dto.response.StorySectionResponse
import com.indianroadmap.story.dto.response.StorySummaryResponse
import com.indianroadmap.story.exception.StoryNotFoundException
import com.indianroadmap.story.service.StoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [StoryController])
class StoryControllerSpec extends Specification {

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    StoryService storyService

    private static StoryResponse storyResponse() {
        new StoryResponse("story-1", "slug", "dest-1", "Title", "Summary", StoryType.CULTURE,
                StoryStatus.DRAFT, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH], [], 1,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"), null)
    }

    def "POST /api/v1/stories returns 201"() {
        given:
        when(storyService.createStory(any())).thenReturn(storyResponse())
        def body = MAPPER.writeValueAsString(new CreateStoryRequest("slug", "dest-1", "Title", "Summary",
                StoryType.CULTURE, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH]))

        expect:
        mockMvc.perform(post("/api/v1/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "GET /api/v1/stories returns 200"() {
        given:
        def summary = new StorySummaryResponse("story-1", "slug", "dest-1", "Title", "Summary", StoryType.CULTURE,
                StoryStatus.DRAFT, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH], 1,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"))
        when(storyService.getStories(any(), any(), any(), any())).thenReturn(new PageImpl([summary]))

        expect:
        mockMvc.perform(get("/api/v1/stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data[0].id').value('story-1'))
    }

    def "GET /api/v1/stories/{id} returns 200"() {
        given:
        when(storyService.getStory(eq("story-1"))).thenReturn(storyResponse())

        expect:
        mockMvc.perform(get("/api/v1/stories/story-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value('story-1'))
    }

    def "GET /api/v1/stories/slug/{slug} returns 200"() {
        given:
        when(storyService.getStoryBySlug(eq("slug"))).thenReturn(storyResponse())

        expect:
        mockMvc.perform(get("/api/v1/stories/slug/slug"))
                .andExpect(status().isOk())
    }

    def "PUT /api/v1/stories/{id} returns 200"() {
        given:
        when(storyService.updateStory(eq("story-1"), any())).thenReturn(storyResponse())
        def body = MAPPER.writeValueAsString(new UpdateStoryRequest("New Title", "Summary",
                StoryDifficulty.DETAILED, [StoryLanguage.ENGLISH]))

        expect:
        mockMvc.perform(put("/api/v1/stories/story-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
    }

    def "DELETE /api/v1/stories/{id} returns 204"() {
        expect:
        mockMvc.perform(delete("/api/v1/stories/story-1"))
                .andExpect(status().isNoContent())
    }

    def "GET /api/v1/stories/destination/{destinationId} returns 200"() {
        given:
        def summary = new StorySummaryResponse("story-1", "slug", "dest-1", "Title", "Summary", StoryType.CULTURE,
                StoryStatus.DRAFT, StoryDifficulty.STANDARD, [StoryLanguage.ENGLISH], 1,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"))
        when(storyService.getStoriesByDestination(eq("dest-1"), any())).thenReturn([summary])

        expect:
        mockMvc.perform(get("/api/v1/stories/destination/dest-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].destinationId').value('dest-1'))
    }

    def "POST /api/v1/stories/{id}/chapters returns 201"() {
        given:
        when(storyService.addChapter(eq("story-1"), any())).thenReturn(storyResponse())
        def body = MAPPER.writeValueAsString(new AddChapterRequest("Origins", 1))

        expect:
        mockMvc.perform(post("/api/v1/stories/story-1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
    }

    def "GET /api/v1/stories/{id}/chapters returns 200"() {
        given:
        def chapter = new StoryChapterResponse("chapter-1", 1, "Origins", [])
        when(storyService.getChapters(eq("story-1"))).thenReturn([chapter])

        expect:
        mockMvc.perform(get("/api/v1/stories/story-1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].chapterId').value('chapter-1'))
    }

    def "POST /api/v1/stories/{id}/chapters/{chapterId}/sections returns 201"() {
        given:
        when(storyService.addSection(eq("story-1"), eq("chapter-1"), any())).thenReturn(storyResponse())
        def body = MAPPER.writeValueAsString(new AddSectionRequest("Heading", "Content", 1, StoryLanguage.ENGLISH))

        expect:
        mockMvc.perform(post("/api/v1/stories/story-1/chapters/chapter-1/sections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
    }

    def "GET /api/v1/stories/{id}/chapters/{chapterId}/sections returns 200"() {
        given:
        def section = new StorySectionResponse("section-1", 1, "Heading", "Content", StoryLanguage.ENGLISH)
        when(storyService.getSections(eq("story-1"), eq("chapter-1"))).thenReturn([section])

        expect:
        mockMvc.perform(get("/api/v1/stories/story-1/chapters/chapter-1/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data[0].sectionId').value('section-1'))
    }

    def "POST /api/v1/stories/{id}/review returns 200"() {
        given:
        when(storyService.submitForReview(eq("story-1"))).thenReturn(storyResponse())

        expect:
        mockMvc.perform(post("/api/v1/stories/story-1/review"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/stories/{id}/publish returns 200"() {
        given:
        when(storyService.publishStory(eq("story-1"))).thenReturn(storyResponse())

        expect:
        mockMvc.perform(post("/api/v1/stories/story-1/publish"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/stories/{id}/archive returns 200"() {
        given:
        when(storyService.archiveStory(eq("story-1"))).thenReturn(storyResponse())

        expect:
        mockMvc.perform(post("/api/v1/stories/story-1/archive"))
                .andExpect(status().isOk())
    }

    def "GET /api/v1/stories/{id} returns 404 for missing story"() {
        given:
        when(storyService.getStory(eq("missing"))).thenThrow(new StoryNotFoundException("missing"))

        expect:
        mockMvc.perform(get("/api/v1/stories/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.error.code').value('STORY_NOT_FOUND'))
    }
}
