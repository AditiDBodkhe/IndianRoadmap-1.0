package com.indianroadmap.roadmap.repository

import com.indianroadmap.roadmap.document.RoadmapDocument
import com.indianroadmap.roadmap.document.RoadmapEdgeDocument
import com.indianroadmap.roadmap.document.RoadmapNodeDocument
import com.indianroadmap.roadmap.document.RoadmapNodeRole
import com.indianroadmap.roadmap.document.RoadmapStatus
import com.indianroadmap.roadmap.document.RoadType
import com.indianroadmap.roadmap.document.RouteDifficulty
import org.junit.jupiter.api.Assumptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class RoadmapRepositoryIntegrationSpec extends Specification {

    @Shared
    static MongoDBContainer mongo

    static {
        try {
            mongo = new MongoDBContainer("mongo:8.0")
            mongo.start()
        } catch (Throwable ignored) {
            mongo = null
        }
    }

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        if (mongo != null) {
            registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl)
        }
    }

    @Autowired
    RoadmapRepository repository

    def setupSpec() {
        Assumptions.assumeTrue(mongo != null, "Docker not available — skipping integration tests")
    }

    def cleanupSpec() {
        if (mongo != null) {
            mongo.stop()
        }
    }

    def cleanup() {
        repository.deleteAll()
    }

    def "save and retrieve roadmap by id"() {
        given:
        def doc = buildRoadmap("great-himalayan-roadmap", "Great Himalayan Roadmap")

        when:
        def saved = repository.save(doc)
        def found = repository.findById(saved.getId())

        then:
        found.present
        found.get().getSlug() == "great-himalayan-roadmap"
        found.get().getStatus() == RoadmapStatus.DRAFT
    }

    def "find by slug"() {
        given:
        repository.save(buildRoadmap("spiti-circuit", "Spiti Circuit"))

        when:
        def result = repository.findBySlug("spiti-circuit")

        then:
        result.present
        result.get().getName() == "Spiti Circuit"
    }

    def "existsBySlug returns true for existing slug"() {
        given:
        repository.save(buildRoadmap("kinnaur-loop", "Kinnaur Loop"))

        expect:
        repository.existsBySlug("kinnaur-loop")
        !repository.existsBySlug("nonexistent")
    }

    def "enforce unique slug constraint"() {
        given:
        repository.save(buildRoadmap("unique-slug", "First"))

        when:
        repository.save(buildRoadmap("unique-slug", "Second"))

        then:
        thrown(Exception)
    }

    def "filter by status with pagination"() {
        given:
        def d1 = buildRoadmap("draft-1", "Draft One")
        d1.setStatus(RoadmapStatus.DRAFT)
        def d2 = buildRoadmap("published-1", "Published One")
        d2.setStatus(RoadmapStatus.PUBLISHED)
        repository.save(d1)
        repository.save(d2)

        when:
        def page = repository.findByStatus(RoadmapStatus.DRAFT, PageRequest.of(0, 10))

        then:
        page.totalElements == 1
        page.content[0].slug == "draft-1"
    }

    def "save roadmap with embedded nodes and edges"() {
        given:
        def doc = buildRoadmap("himalayan-circuit", "Himalayan Circuit")
        def node = new RoadmapNodeDocument("n-001", "dest-001", 1, "Chhitkul", RoadmapNodeRole.START, 3450)
        def edge = new RoadmapEdgeDocument("e-001", "n-001", "n-002", 125.5, 300, RoadType.MOUNTAIN_ROAD, RouteDifficulty.DIFFICULT)
        doc.setNodes([node])
        doc.setEdges([edge])

        when:
        def saved = repository.save(doc)
        def found = repository.findById(saved.getId()).get()

        then:
        found.getNodes().size() == 1
        found.getNodes()[0].getNodeId() == "n-001"
        found.getEdges().size() == 1
        found.getEdges()[0].getDistanceKm() == 125.5
    }

    private static RoadmapDocument buildRoadmap(String slug, String name) {
        def doc = new RoadmapDocument()
        doc.setSlug(slug)
        doc.setName(name)
        doc.setDescription("Test roadmap")
        doc.setStatus(RoadmapStatus.DRAFT)
        doc.setNodes([])
        doc.setEdges([])
        doc.setCreatedAt(Instant.now())
        doc.setUpdatedAt(Instant.now())
        return doc
    }
}
