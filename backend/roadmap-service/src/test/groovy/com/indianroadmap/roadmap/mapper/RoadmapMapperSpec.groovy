package com.indianroadmap.roadmap.mapper

import com.indianroadmap.roadmap.document.*
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RoadmapMapperSpec extends Specification {

    def mapper = new RoadmapMapper()
    def clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    def "maps create request to document with normalized slug"() {
        when:
        def result = mapper.mapToDocument(new CreateRoadmapRequest(" Great Himalayan Roadmap ", " Great Himalayan Roadmap ", "desc", null), clock)

        then:
        result.slug == "great-himalayan-roadmap"
        result.name == "Great Himalayan Roadmap"
        result.status == RoadmapStatus.DRAFT
        result.routeSummary.totalDistanceKm == 0.0
    }

    def "maps document to full response"() {
        given:
        def doc = new RoadmapDocument()
        doc.setId("rm-1")
        doc.setSlug("great-himalayan-roadmap")
        doc.setName("Great Himalayan Roadmap")
        doc.setDescription("desc")
        doc.setStatus(RoadmapStatus.PUBLISHED)
        doc.setNodes([new RoadmapNodeDocument("n1", "d1", 1, "Node 1", RoadmapNodeRole.START, 3000)])
        doc.setEdges([new RoadmapEdgeDocument("e1", "n1", "n2", 12.5, 30, RoadType.HIGHWAY, RouteDifficulty.EASY)])
        doc.setRouteSummary(new RouteSummaryDocument(12.5, 30, 3000, 2500, 500, 1, 1))
        doc.setCreatedAt(Instant.now(clock))
        doc.setUpdatedAt(Instant.now(clock))

        when:
        def result = mapper.mapToResponse(doc)

        then:
        result.id() == "rm-1"
        result.nodes().size() == 1
        result.edges().size() == 1
        result.routeSummary().highestElevationMeters() == 3000
    }

    def "maps document to summary response"() {
        given:
        def doc = new RoadmapDocument()
        doc.setId("rm-1")
        doc.setSlug("slug")
        doc.setName("Name")
        doc.setStatus(RoadmapStatus.DRAFT)
        doc.setNodes([new RoadmapNodeDocument("n1", "d1", 1, "Node 1", RoadmapNodeRole.START, 3000)])
        doc.setRouteSummary(new RouteSummaryDocument(42.0, 120, 3000, 2000, 1000, 1, 0))
        doc.setCreatedAt(Instant.now(clock))

        when:
        def result = mapper.mapToSummaryResponse(doc)

        then:
        result.id() == "rm-1"
        result.nodeCount() == 1
        result.totalDistanceKm() == 42.0d
    }
}
