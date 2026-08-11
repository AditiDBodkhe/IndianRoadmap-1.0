package com.indianroadmap.roadmap.service

import com.indianroadmap.roadmap.client.DestinationClient
import com.indianroadmap.roadmap.client.DestinationSummary
import com.indianroadmap.roadmap.document.*
import com.indianroadmap.roadmap.dto.request.*
import com.indianroadmap.roadmap.dto.response.*
import com.indianroadmap.roadmap.exception.*
import com.indianroadmap.roadmap.mapper.RoadmapMapper
import com.indianroadmap.roadmap.repository.RoadmapRepository
import com.indianroadmap.roadmap.validation.RoadmapStructureValidator
import com.indianroadmap.roadmap.validation.RoadmapValidator
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RoadmapServiceSpec extends Specification {

    def repo = Mock(RoadmapRepository)
    def mapper = Mock(RoadmapMapper)
    def client = Mock(DestinationClient)
    def validator = Mock(RoadmapValidator)
    def structureValidator = Mock(RoadmapStructureValidator)
    def clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Subject
    RoadmapService service = new RoadmapServiceImpl(repo, mapper, client, validator, structureValidator, clock)

    def "createRoadmap creates and saves roadmap"() {
        given:
        def request = new CreateRoadmapRequest("great-himalayan-roadmap", "Great Himalayan Roadmap", "A journey", null)
        def doc = new RoadmapDocument()
        doc.setId("roadmap-001")
        doc.setSlug("great-himalayan-roadmap")
        doc.setName("Great Himalayan Roadmap")
        doc.setStatus(RoadmapStatus.DRAFT)
        doc.setNodes([])
        doc.setEdges([])
        doc.setRouteSummary(new RouteSummaryDocument())
        doc.setCreatedAt(Instant.now(clock))
        doc.setUpdatedAt(Instant.now(clock))

        def response = new RoadmapResponse("roadmap-001", "great-himalayan-roadmap", "Great Himalayan Roadmap", "A journey", RoadmapStatus.DRAFT, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.existsBySlug("great-himalayan-roadmap") >> false
        validator.normalizeSlug("great-himalayan-roadmap") >> "great-himalayan-roadmap"
        mapper.mapToDocument(_ as CreateRoadmapRequest, clock) >> doc
        repo.save(doc) >> doc
        mapper.mapToResponse(doc) >> response

        then:
        def result = service.createRoadmap(request)
        result != null
        result.id() == "roadmap-001"
    }

    def "createRoadmap throws DuplicateRoadmapException for duplicate slug"() {
        given:
        def request = new CreateRoadmapRequest("existing-slug", "Name", null, null)

        when:
        validator.normalizeSlug("existing-slug") >> "existing-slug"
        repo.existsBySlug("existing-slug") >> true
        service.createRoadmap(request)

        then:
        thrown(DuplicateRoadmapException)
    }

    def "getRoadmap returns roadmap by id"() {
        given:
        def doc = new RoadmapDocument()
        doc.setId("roadmap-001")
        def response = new RoadmapResponse("roadmap-001", "slug", "Name", null, RoadmapStatus.DRAFT, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.findById("roadmap-001") >> Optional.of(doc)
        mapper.mapToResponse(doc) >> response

        then:
        def result = service.getRoadmap("roadmap-001")
        result.id() == "roadmap-001"
    }

    def "getRoadmap throws RoadmapNotFoundException for unknown id"() {
        when:
        repo.findById("unknown-id") >> Optional.empty()
        service.getRoadmap("unknown-id")

        then:
        thrown(RoadmapNotFoundException)
    }

    def "addNode adds node to roadmap with elevation snapshot"() {
        given:
        def roadmap = new RoadmapDocument()
        roadmap.setId("roadmap-001")
        roadmap.setNodes(new ArrayList<>())
        roadmap.setEdges(new ArrayList<>())
        roadmap.setStatus(RoadmapStatus.DRAFT)

        def request = new AddRoadmapNodeRequest("dest-001", 1, "Chhitkul", RoadmapNodeRole.START)
        def destSummary = new DestinationSummary("dest-001", "chhitkul", "Chhitkul", 31.5, 78.6, 3450)
        def savedRoadmap = new RoadmapDocument()
        savedRoadmap.setId("roadmap-001")
        savedRoadmap.setNodes([new RoadmapNodeDocument("node-001", "dest-001", 1, "Chhitkul", RoadmapNodeRole.START, 3450)])
        savedRoadmap.setEdges([])
        def response = new RoadmapResponse("roadmap-001", "slug", "Name", null, RoadmapStatus.DRAFT, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.findById("roadmap-001") >> Optional.of(roadmap)
        client.getDestination("dest-001") >> destSummary
        repo.save(_ as RoadmapDocument) >> savedRoadmap
        mapper.mapToResponse(_ as RoadmapDocument) >> response

        then:
        def result = service.addNode("roadmap-001", request)
        result != null
    }

    def "addNode throws DestinationNotFoundException when destination missing"() {
        given:
        def roadmap = new RoadmapDocument()
        roadmap.setId("roadmap-001")
        roadmap.setNodes(new ArrayList<>())
        roadmap.setEdges(new ArrayList<>())
        def request = new AddRoadmapNodeRequest("bad-dest", 1, "Label", RoadmapNodeRole.WAYPOINT)

        when:
        repo.findById("roadmap-001") >> Optional.of(roadmap)
        client.getDestination("bad-dest") >> { throw new DestinationNotFoundException("bad-dest") }
        service.addNode("roadmap-001", request)

        then:
        thrown(DestinationNotFoundException)
    }

    def "reorderNodes assigns sequences correctly"() {
        given:
        def nodeA = new RoadmapNodeDocument("node-A", "dest-A", 1, "A", RoadmapNodeRole.START, 1000)
        def nodeB = new RoadmapNodeDocument("node-B", "dest-B", 2, "B", RoadmapNodeRole.WAYPOINT, 1500)
        def nodeC = new RoadmapNodeDocument("node-C", "dest-C", 3, "C", RoadmapNodeRole.END, 1200)
        def roadmap = new RoadmapDocument()
        roadmap.setId("roadmap-001")
        roadmap.setNodes(new ArrayList<>([nodeA, nodeB, nodeC]))
        roadmap.setEdges(new ArrayList<>())

        def request = new ReorderRoadmapNodesRequest(["node-C", "node-A", "node-B"])
        def response = new RoadmapResponse("roadmap-001", "slug", "Name", null, RoadmapStatus.DRAFT, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.findById("roadmap-001") >> Optional.of(roadmap)
        repo.save(_ as RoadmapDocument) >> { RoadmapDocument d -> d }
        mapper.mapToResponse(_ as RoadmapDocument) >> response

        then:
        def result = service.reorderNodes("roadmap-001", request)
        result != null
    }

    @Unroll
    def "status transition from #from to #to is #valid"() {
        def localValidator = new com.indianroadmap.roadmap.validation.RoadmapValidator()

        when:
        localValidator.validateStatusTransition(from, to)

        then:
        noExceptionThrown()

        where:
        from                    | to                      | valid
        RoadmapStatus.DRAFT     | RoadmapStatus.PUBLISHED | true
        RoadmapStatus.DRAFT     | RoadmapStatus.ARCHIVED  | true
        RoadmapStatus.PUBLISHED | RoadmapStatus.ARCHIVED  | true
    }

    def "invalid status transition throws exception"() {
        when:
        new com.indianroadmap.roadmap.validation.RoadmapValidator()
            .validateStatusTransition(RoadmapStatus.ARCHIVED, RoadmapStatus.PUBLISHED)

        then:
        thrown(InvalidRoadmapStatusException)
    }

    def "route summary calculates correctly"() {
        given:
        def nodeA = new RoadmapNodeDocument("n1", "d1", 1, "A", RoadmapNodeRole.START, 1000)
        def nodeB = new RoadmapNodeDocument("n2", "d2", 2, "B", RoadmapNodeRole.WAYPOINT, 1500)
        def nodeC = new RoadmapNodeDocument("n3", "d3", 3, "C", RoadmapNodeRole.WAYPOINT, 1200)
        def nodeD = new RoadmapNodeDocument("n4", "d4", 4, "D", RoadmapNodeRole.END, 2000)

        def edgeAB = new RoadmapEdgeDocument("e1", "n1", "n2", 100.0, 120, RoadType.MOUNTAIN_ROAD, RouteDifficulty.DIFFICULT)
        def edgeBC = new RoadmapEdgeDocument("e2", "n2", "n3", 150.0, 180, RoadType.MOUNTAIN_ROAD, RouteDifficulty.DIFFICULT)
        def edgeCD = new RoadmapEdgeDocument("e3", "n3", "n4", 80.0, 100, RoadType.MOUNTAIN_ROAD, RouteDifficulty.EXTREME)

        def roadmap = new RoadmapDocument()
        roadmap.setNodes(new ArrayList<>([nodeA, nodeB, nodeC, nodeD]))
        roadmap.setEdges(new ArrayList<>([edgeAB, edgeBC, edgeCD]))
        roadmap.setId("rm-test")
        roadmap.setStatus(RoadmapStatus.DRAFT)

        expect:
        def impl = (RoadmapServiceImpl) service
        def summary = impl.calculateRouteSummary(roadmap)
        summary.getTotalDistanceKm() == 330.0
        summary.getHighestElevationMeters() == 2000
        summary.getLowestElevationMeters() == 1000
        summary.getElevationGainMeters() == 1300
        summary.getNodeCount() == 4
        summary.getEdgeCount() == 3
    }

    def "publishRoadmap publishes DRAFT roadmap with valid structure"() {
        given:
        def nodeA = new RoadmapNodeDocument("n1", "d1", 1, "A", RoadmapNodeRole.START, 1000)
        def nodeB = new RoadmapNodeDocument("n2", "d2", 2, "B", RoadmapNodeRole.END, 2000)
        def edge = new RoadmapEdgeDocument("e1", "n1", "n2", 100.0, 120, RoadType.HIGHWAY, RouteDifficulty.EASY)
        def roadmap = new RoadmapDocument()
        roadmap.setId("rm-001")
        roadmap.setStatus(RoadmapStatus.DRAFT)
        roadmap.setNodes(new ArrayList<>([nodeA, nodeB]))
        roadmap.setEdges(new ArrayList<>([edge]))
        def response = new RoadmapResponse("rm-001", "slug", "Name", null, RoadmapStatus.PUBLISHED, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.findById("rm-001") >> Optional.of(roadmap)
        repo.save(_ as RoadmapDocument) >> roadmap
        mapper.mapToResponse(roadmap) >> response

        then:
        def result = service.publishRoadmap("rm-001")
        result != null
    }

    def "archiveRoadmap archives roadmap"() {
        given:
        def roadmap = new RoadmapDocument()
        roadmap.setId("rm-001")
        roadmap.setStatus(RoadmapStatus.PUBLISHED)
        roadmap.setNodes([])
        roadmap.setEdges([])
        def response = new RoadmapResponse("rm-001", "slug", "Name", null, RoadmapStatus.ARCHIVED, [], [], null, Instant.now(clock), Instant.now(clock))

        when:
        repo.findById("rm-001") >> Optional.of(roadmap)
        repo.save(_ as RoadmapDocument) >> roadmap
        mapper.mapToResponse(roadmap) >> response

        then:
        def result = service.archiveRoadmap("rm-001")
        result.status() == RoadmapStatus.ARCHIVED
    }

    def "deleteRoadmap throws exception for nonexistent roadmap"() {
        when:
        repo.findById("no-such-id") >> Optional.empty()
        service.deleteRoadmap("no-such-id")

        then:
        thrown(RoadmapNotFoundException)
    }
}
