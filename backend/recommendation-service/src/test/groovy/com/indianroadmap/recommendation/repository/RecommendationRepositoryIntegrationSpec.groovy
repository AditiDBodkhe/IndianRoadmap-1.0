package com.indianroadmap.recommendation.repository

import com.indianroadmap.recommendation.document.*
import org.junit.jupiter.api.Assumptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class RecommendationRepositoryIntegrationSpec extends Specification {

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
    RecommendationProfileRepository repository

    def setupSpec() {
        Assumptions.assumeTrue(mongo != null, "Docker not available — skipping integration tests")
    }

    def setup() {
        repository.deleteAll()
    }

    def "save and retrieve profile by destinationId"() {
        given:
        def doc = buildProfile("hanle", [Mood.ZEN, Mood.SOLITUDE], [Interest.ASTRONOMY])

        when:
        repository.save(doc)
        def found = repository.findByDestinationId("hanle")

        then:
        found.present
        found.get().destinationId == "hanle"
        found.get().moods.contains(Mood.ZEN)
        found.get().interests.contains(Interest.ASTRONOMY)
    }

    def "findByMoodsContaining returns matching profiles"() {
        given:
        repository.save(buildProfile("hanle", [Mood.ZEN], []))
        repository.save(buildProfile("tabo", [Mood.SPIRITUAL], []))

        when:
        def results = repository.findByMoodsContaining(Mood.ZEN)

        then:
        results.size() == 1
        results[0].destinationId == "hanle"
    }

    def "findByInterestsContaining returns matching profiles"() {
        given:
        repository.save(buildProfile("hanle", [], [Interest.ASTRONOMY]))
        repository.save(buildProfile("tabo", [], [Interest.SPIRITUALITY]))

        when:
        def results = repository.findByInterestsContaining(Interest.ASTRONOMY)

        then:
        results.size() == 1
        results[0].destinationId == "hanle"
    }

    def "findByRegionsContaining returns matching profiles"() {
        given:
        def doc = buildProfile("hanle", [], [])
        doc.regions = ["LADAKH", "HIMALAYAS"]
        repository.save(doc)
        repository.save(buildProfile("tabo", [], []))

        when:
        def results = repository.findByRegionsContaining("LADAKH")

        then:
        results.size() == 1
        results[0].destinationId == "hanle"
    }

    def "update profile preserves destinationId"() {
        given:
        def doc = buildProfile("hanle", [Mood.ZEN], [])
        def saved = repository.save(doc)
        saved.moods = [Mood.ADVENTUROUS]

        when:
        def updated = repository.save(saved)

        then:
        updated.destinationId == "hanle"
        updated.moods == [Mood.ADVENTUROUS]
    }

    def "delete profile by destinationId"() {
        given:
        repository.save(buildProfile("hanle", [Mood.ZEN], []))

        when:
        repository.deleteByDestinationId("hanle")

        then:
        repository.findByDestinationId("hanle").empty
    }

    def "existsByDestinationId returns true for existing profile"() {
        given:
        repository.save(buildProfile("hanle", [], []))

        expect:
        repository.existsByDestinationId("hanle")
        !repository.existsByDestinationId("unknown")
    }

    def "findAll returns all saved profiles"() {
        given:
        repository.save(buildProfile("hanle", [], []))
        repository.save(buildProfile("tabo", [], []))
        repository.save(buildProfile("kaza", [], []))

        when:
        def all = repository.findAll()

        then:
        all.size() == 3
    }

    // ── Helper ──

    private RecommendationProfileDocument buildProfile(String destId, List<Mood> moods, List<Interest> interests) {
        def doc = new RecommendationProfileDocument()
        doc.destinationId = destId
        doc.moods = moods
        doc.interests = interests
        doc.travelStyles = []
        doc.regions = []
        doc.seasonTags = []
        doc.idealDurationMin = 2
        doc.idealDurationMax = 5
        doc.budgetMin = 5000
        doc.budgetMax = 20000
        doc.weight = 1.0
        doc.difficulty = "MODERATE"
        doc.createdAt = Instant.now()
        doc.updatedAt = Instant.now()
        doc
    }
}
