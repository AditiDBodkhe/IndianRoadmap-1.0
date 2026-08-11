package com.indianroadmap.destination.repository

import com.indianroadmap.destination.document.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.NearQuery
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Title("DestinationRepository — MongoDB integration tests")
class DestinationRepositoryIntegrationSpec extends Specification {

    @Shared
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")

    static {
        mongoDBContainer.start()
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
    }

    @Autowired
    DestinationRepository repository

    @Autowired
    MongoTemplate mongoTemplate

    def cleanup() {
        repository.deleteAll()
    }

    def "saves and retrieves a destination by id"() {
        given:
        def doc = buildDestination("chhitkul", "Chhitkul", "Himachal Pradesh", "Kinnaur", 78.44, 31.35, 3450)

        when:
        def saved = repository.save(doc)
        def found = repository.findById(saved.getId())

        then:
        found.isPresent()
        found.get().getSlug() == "chhitkul"
        found.get().getState() == "Himachal Pradesh"
    }

    def "finds destination by slug"() {
        given:
        repository.save(buildDestination("tabo", "Tabo", "Himachal Pradesh", "Kinnaur", 78.39, 32.09, 3280))

        when:
        def result = repository.findBySlug("tabo")

        then:
        result.isPresent()
        result.get().getSlug() == "tabo"
    }

    def "existsBySlug returns true for existing slug"() {
        given:
        repository.save(buildDestination("kaza", "Kaza", "Himachal Pradesh", "Lahaul and Spiti", 78.07, 32.22, 3800))

        expect:
        repository.existsBySlug("kaza")
    }

    def "existsBySlug returns false for non-existent slug"() {
        expect:
        !repository.existsBySlug("nonexistent")
    }

    def "enforces unique slug constraint"() {
        given:
        repository.save(buildDestination("hanle", "Hanle", "Ladakh", "Leh", 78.96, 32.77, 4500))

        when:
        repository.save(buildDestination("hanle", "Hanle2", "Ladakh", "Leh", 79.0, 32.8, 4500))

        then:
        thrown(Exception)
    }

    def "performs geospatial nearby query"() {
        given:
        repository.save(buildDestination("kaza-geo", "Kaza", "HP", "Spiti", 78.07, 32.22, 3800))
        repository.save(buildDestination("tabo-geo", "Tabo", "HP", "Spiti", 78.39, 32.09, 3280))
        repository.save(buildDestination("amritsar-geo", "Amritsar", "Punjab", "Amritsar", 74.87, 31.63, 230))

        when:
        def point = new GeoJsonPoint(78.07, 32.22)
        def nearQuery = NearQuery.near(point)
                .maxDistance(new Distance(50, Metrics.KILOMETERS))
                .spherical(true)
        def results = mongoTemplate.geoNear(nearQuery, DestinationDocument.class)

        then:
        results.getContent().size() >= 1
        results.getContent().any { it.content.slug.contains("kaza") }
    }

    private static DestinationDocument buildDestination(
            String slug, String name, String state, String region,
            double longitude, double latitude, int elevationMeters) {
        def doc = new DestinationDocument()
        doc.setSlug(slug)
        doc.setName(new DestinationName(name, name))
        doc.setState(state)
        doc.setRegion(region)
        doc.setDistrict(region)
        doc.setShortDescription("")
        doc.setDescription("")
        doc.setCoordinates(new GeoJsonPoint(longitude, latitude))
        doc.setElevation(new Elevation(elevationMeters, (int)(elevationMeters * 3.28084)))
        doc.setCategories([DestinationCategory.VILLAGE])
        doc.setMoods([Mood.ADVENTURE])
        doc.setLanguages([Language.HINDI])
        doc.setHistoricalHighlights([])
        doc.setAttractions([])
        doc.setImages([])
        doc.setSources([])
        doc.setVerified(false)
        doc.setCreatedAt(Instant.now())
        doc.setUpdatedAt(Instant.now())
        return doc
    }
}
