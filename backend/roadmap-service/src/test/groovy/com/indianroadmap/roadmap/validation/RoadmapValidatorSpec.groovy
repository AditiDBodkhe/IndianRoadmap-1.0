package com.indianroadmap.roadmap.validation

import com.indianroadmap.roadmap.document.*
import com.indianroadmap.roadmap.dto.request.AddRoadmapEdgeRequest
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest
import com.indianroadmap.roadmap.exception.InvalidRoadmapEdgeException
import com.indianroadmap.roadmap.exception.InvalidRoadmapException
import com.indianroadmap.roadmap.exception.InvalidRoadmapStatusException
import spock.lang.Specification

class RoadmapValidatorSpec extends Specification {

    def validator = new RoadmapValidator()
    def structureValidator = new RoadmapStructureValidator()

    def "blank slug throws exception"() {
        when:
        validator.validateCreateRequest(new CreateRoadmapRequest(" ", "Name", null, null))

        then:
        thrown(InvalidRoadmapException)
    }

    def "blank name throws exception"() {
        when:
        validator.validateCreateRequest(new CreateRoadmapRequest("slug", " ", null, null))

        then:
        thrown(InvalidRoadmapException)
    }

    def "invalid status transition throws"() {
        when:
        validator.validateStatusTransition(RoadmapStatus.ARCHIVED, RoadmapStatus.PUBLISHED)

        then:
        thrown(InvalidRoadmapStatusException)
    }

    def "valid status transitions do not throw"() {
        expect:
        validator.validateStatusTransition(RoadmapStatus.DRAFT, RoadmapStatus.PUBLISHED)
        validator.validateStatusTransition(RoadmapStatus.DRAFT, RoadmapStatus.ARCHIVED)
        validator.validateStatusTransition(RoadmapStatus.PUBLISHED, RoadmapStatus.ARCHIVED)
    }

    def "edge self loop detection throws"() {
        given:
        def roadmap = new RoadmapDocument()
        roadmap.setNodes([new RoadmapNodeDocument("n1", "d1", 1, "A", RoadmapNodeRole.START, 1000)])

        when:
        structureValidator.validateEdge(roadmap, new AddRoadmapEdgeRequest("n1", "n1", 10.0d, 20, RoadType.HIGHWAY, RouteDifficulty.EASY))

        then:
        thrown(InvalidRoadmapEdgeException)
    }

    def "duplicate edge detection throws"() {
        given:
        def roadmap = new RoadmapDocument()
        roadmap.setNodes([
            new RoadmapNodeDocument("n1", "d1", 1, "A", RoadmapNodeRole.START, 1000),
            new RoadmapNodeDocument("n2", "d2", 2, "B", RoadmapNodeRole.END, 2000)
        ])
        roadmap.setEdges([new RoadmapEdgeDocument("e1", "n1", "n2", 10.0d, 20, RoadType.HIGHWAY, RouteDifficulty.EASY)])

        when:
        structureValidator.validateEdge(roadmap, new AddRoadmapEdgeRequest("n1", "n2", 15.0d, 25, RoadType.HIGHWAY, RouteDifficulty.EASY))

        then:
        thrown(InvalidRoadmapEdgeException)
    }
}
