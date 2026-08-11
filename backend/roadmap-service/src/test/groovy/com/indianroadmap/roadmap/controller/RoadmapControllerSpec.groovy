package com.indianroadmap.roadmap.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.indianroadmap.roadmap.document.RoadType
import com.indianroadmap.roadmap.document.RouteDifficulty
import com.indianroadmap.roadmap.document.RoadmapNodeRole
import com.indianroadmap.roadmap.document.RoadmapStatus
import com.indianroadmap.roadmap.dto.request.AddRoadmapEdgeRequest
import com.indianroadmap.roadmap.dto.request.AddRoadmapNodeRequest
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest
import com.indianroadmap.roadmap.dto.response.RoadmapNodeResponse
import com.indianroadmap.roadmap.dto.response.RoadmapResponse
import com.indianroadmap.roadmap.dto.response.RoadmapSummaryResponse
import com.indianroadmap.roadmap.dto.response.RouteSummaryResponse
import com.indianroadmap.roadmap.exception.RoadmapNotFoundException
import com.indianroadmap.roadmap.service.RoadmapService
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

@WebMvcTest(controllers = [RoadmapController])
class RoadmapControllerSpec extends Specification {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    RoadmapService roadmapService

    private static RoadmapResponse roadmapResponse() {
        new RoadmapResponse("rm-1", "slug", "Name", "desc", RoadmapStatus.DRAFT, [], [],
                new RouteSummaryResponse(0, 0, 0, 0, 0, 0, 0),
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"))
    }

    def "POST /api/v1/roadmaps returns 201"() {
        given:
        when(roadmapService.createRoadmap(any())).thenReturn(roadmapResponse())
        def body = MAPPER.writeValueAsString(new CreateRoadmapRequest("my-slug", "Name", "desc", null))

        expect:
        mockMvc.perform(post("/api/v1/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "GET /api/v1/roadmaps returns 200"() {
        given:
        def summary = new RoadmapSummaryResponse("rm-1", "slug", "Name", "desc", RoadmapStatus.DRAFT, 0, 0, 0.0d,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"))
        when(roadmapService.getRoadmaps(any(), any())).thenReturn(new PageImpl([summary]))

        expect:
        mockMvc.perform(get("/api/v1/roadmaps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "GET /api/v1/roadmaps/{id} returns 200"() {
        given:
        when(roadmapService.getRoadmap(eq("rm-1"))).thenReturn(roadmapResponse())

        expect:
        mockMvc.perform(get("/api/v1/roadmaps/rm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value('rm-1'))
    }

    def "GET /api/v1/roadmaps/slug/{slug} returns 200"() {
        given:
        when(roadmapService.getRoadmapBySlug(eq("my-slug"))).thenReturn(roadmapResponse())

        expect:
        mockMvc.perform(get("/api/v1/roadmaps/slug/my-slug"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/roadmaps/{id}/nodes returns 201"() {
        given:
        when(roadmapService.addNode(eq("rm-1"), any())).thenReturn(roadmapResponse())
        def body = MAPPER.writeValueAsString(new AddRoadmapNodeRequest("dest-1", 1, "Node", RoadmapNodeRole.START))

        expect:
        mockMvc.perform(post("/api/v1/roadmaps/rm-1/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
    }

    def "GET /api/v1/roadmaps/{id}/nodes returns 200"() {
        given:
        def node = new RoadmapNodeResponse("n1", "d1", 1, "Node", RoadmapNodeRole.START, 1000)
        when(roadmapService.getNodes(eq("rm-1"))).thenReturn([node])

        expect:
        mockMvc.perform(get("/api/v1/roadmaps/rm-1/nodes"))
                .andExpect(status().isOk())
    }

    def "DELETE /api/v1/roadmaps/{id}/nodes/{nodeId} returns 200"() {
        given:
        when(roadmapService.removeNode(eq("rm-1"), eq("n-1"))).thenReturn(roadmapResponse())

        expect:
        mockMvc.perform(delete("/api/v1/roadmaps/rm-1/nodes/n-1"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/roadmaps/{id}/edges returns 201"() {
        given:
        when(roadmapService.addEdge(eq("rm-1"), any())).thenReturn(roadmapResponse())
        def body = MAPPER.writeValueAsString(new AddRoadmapEdgeRequest("n1", "n2", 1.5d, 20, RoadType.HIGHWAY, RouteDifficulty.EASY))

        expect:
        mockMvc.perform(post("/api/v1/roadmaps/rm-1/edges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
    }

    def "GET /api/v1/roadmaps/{id}/edges returns 200"() {
        given:
        when(roadmapService.getEdges(eq("rm-1"))).thenReturn([])

        expect:
        mockMvc.perform(get("/api/v1/roadmaps/rm-1/edges"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/roadmaps/{id}/publish returns 200"() {
        given:
        when(roadmapService.publishRoadmap(eq("rm-1"))).thenReturn(roadmapResponse())

        expect:
        mockMvc.perform(post("/api/v1/roadmaps/rm-1/publish"))
                .andExpect(status().isOk())
    }

    def "POST /api/v1/roadmaps/{id}/archive returns 200"() {
        given:
        when(roadmapService.archiveRoadmap(eq("rm-1"))).thenReturn(roadmapResponse())

        expect:
        mockMvc.perform(post("/api/v1/roadmaps/rm-1/archive"))
                .andExpect(status().isOk())
    }

    def "GET /api/v1/roadmaps/{id} returns 404 for missing roadmap"() {
        given:
        when(roadmapService.getRoadmap(eq("missing"))).thenThrow(new RoadmapNotFoundException("missing"))

        expect:
        mockMvc.perform(get("/api/v1/roadmaps/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.error.code').value('ROADMAP_NOT_FOUND'))
    }

    def "POST /api/v1/roadmaps with blank name returns 400"() {
        expect:
        mockMvc.perform(post("/api/v1/roadmaps")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"slug":"valid","name":"","description":"d"}'))
                .andExpect(status().isBadRequest())
    }
}
