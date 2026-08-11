package com.indianroadmap.recommendation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.indianroadmap.recommendation.document.MatchLevel
import com.indianroadmap.recommendation.document.Mood
import com.indianroadmap.recommendation.dto.request.RecommendationRequest
import com.indianroadmap.recommendation.dto.response.DestinationSummaryDto
import com.indianroadmap.recommendation.dto.response.RecommendationResponse
import com.indianroadmap.recommendation.exception.GlobalExceptionHandler
import com.indianroadmap.recommendation.service.RecommendationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [RecommendationController])
@Import(GlobalExceptionHandler)
class RecommendationControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    RecommendationService recommendationService

    private static final ObjectMapper MAPPER = new ObjectMapper()

    def "POST /api/v1/recommendations returns 200 with results"() {
        given:
        def request = [mood: "ZEN", interests: ["NATURE"], travelStyle: "SLOW_TRAVEL",
                       durationDays: 5, maxBudget: 30000, preferredRegion: "LADAKH",
                       season: "SUMMER", limit: 5]
        def dest = new DestinationSummaryDto("hanle", "hanle", "Hanle", "Leh", "LADAKH", [], [])
        def responses = [new RecommendationResponse(dest, 91.5, MatchLevel.EXCELLENT,
                ["Matches your Zen mood"], [Mood.ZEN], [], [])]
        when(recommendationService.recommend(any())).thenReturn(responses)

        when:
        def result = mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.success').value(true))
              .andExpect(jsonPath('$.data[0].destination.id').value("hanle"))
              .andExpect(jsonPath('$.data[0].score').value(91.5))
              .andExpect(jsonPath('$.data[0].matchLevel').value("EXCELLENT"))
    }

    def "POST /api/v1/recommendations with null mood returns 400"() {
        given:
        def request = [interests: ["NATURE"], limit: 5]

        when:
        def result = mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))

        then:
        result.andExpect(status().isBadRequest())
              .andExpect(jsonPath('$.success').value(false))
              .andExpect(jsonPath('$.error.code').value("VALIDATION_ERROR"))
    }

    def "GET /api/v1/recommendations/mood/ZEN returns 200"() {
        given:
        def dest = new DestinationSummaryDto("hanle", "hanle", "Hanle", "Leh", "LADAKH", [], [])
        def responses = [new RecommendationResponse(dest, 80.0, MatchLevel.VERY_GOOD,
                ["Matches ZEN"], [Mood.ZEN], [], [])]
        when(recommendationService.recommendByMood(eq(Mood.ZEN), anyInt())).thenReturn(responses)

        when:
        def result = mockMvc.perform(get("/api/v1/recommendations/mood/ZEN"))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.data[0].matchLevel').value("VERY_GOOD"))
    }

    def "GET /api/v1/recommendations/destination/{id}/similar returns 200"() {
        given:
        def dest = new DestinationSummaryDto("tabo", "tabo", "Tabo", "Spiti", "HIMALAYAS", [], [])
        def responses = [new RecommendationResponse(dest, 75.0, MatchLevel.VERY_GOOD,
                ["Similar region"], [], [], [])]
        when(recommendationService.findSimilarDestinations(eq("hanle"), anyInt())).thenReturn(responses)

        when:
        def result = mockMvc.perform(get("/api/v1/recommendations/destination/hanle/similar"))

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.data').isArray())
    }

    def "POST /api/v1/recommendations with limit over 50 returns 400"() {
        given:
        def request = [mood: "ZEN", limit: 100]

        when:
        def result = mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))

        then:
        result.andExpect(status().isBadRequest())
    }
}
