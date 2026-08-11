package com.indianroadmap.recommendation.service

import com.indianroadmap.recommendation.client.DestinationClient
import com.indianroadmap.recommendation.client.DestinationSummary
import com.indianroadmap.recommendation.document.*
import com.indianroadmap.recommendation.dto.request.RecommendationProfileRequest
import com.indianroadmap.recommendation.dto.request.RecommendationRequest
import com.indianroadmap.recommendation.dto.response.DestinationSummaryDto
import com.indianroadmap.recommendation.dto.response.RecommendationProfileResponse
import com.indianroadmap.recommendation.dto.response.RecommendationResponse
import com.indianroadmap.recommendation.engine.RecommendationEngine
import com.indianroadmap.recommendation.exception.DestinationNotFoundException
import com.indianroadmap.recommendation.exception.RecommendationProfileNotFoundException
import com.indianroadmap.recommendation.mapper.RecommendationMapper
import com.indianroadmap.recommendation.repository.RecommendationProfileRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant

class RecommendationServiceSpec extends Specification {

    RecommendationEngine engine = Mock()
    RecommendationProfileRepository profileRepository = Mock()
    DestinationClient destinationClient = Mock()
    RecommendationMapper mapper = new RecommendationMapper(Clock.systemUTC())

    @Subject
    RecommendationService service = new RecommendationServiceImpl(engine, profileRepository, destinationClient, mapper)

    // ── recommend() ──────────────────────────────────────

    def "recommend delegates to engine"() {
        given:
        def request = new RecommendationRequest(Mood.ZEN, null, null, null, null, null, null, 10)
        def expected = [buildRecommendationResponse("hanle")]

        when:
        def result = service.recommend(request)

        then:
        1 * engine.recommend(request) >> expected
        result == expected
    }

    def "recommendByMood builds request with mood and delegates to engine"() {
        given:
        def responses = [buildRecommendationResponse("hanle"), buildRecommendationResponse("tabo")]

        when:
        def result = service.recommendByMood(Mood.SPIRITUAL, 5)

        then:
        1 * engine.recommend({ RecommendationRequest r ->
            r.mood() == Mood.SPIRITUAL && r.limit() == 5
        }) >> responses
        result.size() == 2
    }

    // ── createProfile() ──────────────────────────────────

    def "createProfile verifies destination exists before saving"() {
        given:
        def request = buildProfileRequest("hanle")
        def dest = buildDestinationSummary("hanle")
        def doc = new RecommendationProfileDocument(id: "p1", destinationId: "hanle",
                moods: [Mood.ZEN], interests: [], travelStyles: [],
                regions: [], seasonTags: [], createdAt: Instant.now(), updatedAt: Instant.now())

        when:
        def result = service.createProfile(request)

        then:
        1 * destinationClient.getDestination("hanle") >> Optional.of(dest)
        1 * profileRepository.save(_) >> doc
        result.destinationId() == "hanle"
    }

    def "createProfile throws DestinationNotFoundException for unknown destination"() {
        given:
        def request = buildProfileRequest("unknown")

        when:
        service.createProfile(request)

        then:
        1 * destinationClient.getDestination("unknown") >> Optional.empty()
        thrown(DestinationNotFoundException)
    }

    // ── getProfile() ──────────────────────────────────────

    def "getProfile returns profile for existing destination"() {
        given:
        def doc = new RecommendationProfileDocument(id: "p1", destinationId: "hanle",
                moods: [Mood.ZEN], interests: [], travelStyles: [],
                regions: [], seasonTags: [], createdAt: Instant.now(), updatedAt: Instant.now())

        when:
        def result = service.getProfile("hanle")

        then:
        1 * profileRepository.findByDestinationId("hanle") >> Optional.of(doc)
        result.destinationId() == "hanle"
    }

    def "getProfile throws RecommendationProfileNotFoundException for missing profile"() {
        when:
        service.getProfile("nonexistent")

        then:
        1 * profileRepository.findByDestinationId("nonexistent") >> Optional.empty()
        thrown(RecommendationProfileNotFoundException)
    }

    // ── updateProfile() ───────────────────────────────────

    def "updateProfile updates existing profile"() {
        given:
        def doc = new RecommendationProfileDocument(id: "p1", destinationId: "hanle",
                moods: [Mood.ZEN], interests: [], travelStyles: [],
                regions: [], seasonTags: [], createdAt: Instant.now(), updatedAt: Instant.now())
        def request = buildProfileRequest("hanle")

        when:
        def result = service.updateProfile("hanle", request)

        then:
        1 * profileRepository.findByDestinationId("hanle") >> Optional.of(doc)
        1 * profileRepository.save(_) >> { args -> args[0] as RecommendationProfileDocument }
        result != null
    }

    def "updateProfile throws when profile not found"() {
        when:
        service.updateProfile("missing", buildProfileRequest("missing"))

        then:
        1 * profileRepository.findByDestinationId("missing") >> Optional.empty()
        thrown(RecommendationProfileNotFoundException)
    }

    // ── deleteProfile() ───────────────────────────────────

    def "deleteProfile deletes existing profile"() {
        when:
        service.deleteProfile("hanle")

        then:
        1 * profileRepository.existsByDestinationId("hanle") >> true
        1 * profileRepository.deleteByDestinationId("hanle")
    }

    def "deleteProfile throws when profile not found"() {
        when:
        service.deleteProfile("missing")

        then:
        1 * profileRepository.existsByDestinationId("missing") >> false
        thrown(RecommendationProfileNotFoundException)
    }

    // ── findSimilarDestinations() ─────────────────────────

    def "findSimilarDestinations excludes the reference destination from results"() {
        given:
        def refProfile = new RecommendationProfileDocument(
                destinationId: "hanle", moods: [Mood.ZEN], interests: [],
                travelStyles: [], regions: ["LADAKH"], seasonTags: [])
        def results = [buildRecommendationResponse("tabo"), buildRecommendationResponse("hanle")]

        when:
        def similar = service.findSimilarDestinations("hanle", 5)

        then:
        1 * profileRepository.findByDestinationId("hanle") >> Optional.of(refProfile)
        1 * engine.recommend(_) >> results
        !similar.any { it.destination().id() == "hanle" }
    }

    // ── Helpers ──────────────────────────────────────────────

    private RecommendationProfileRequest buildProfileRequest(String destId) {
        new RecommendationProfileRequest(destId, [Mood.ZEN], [], [], ["LADAKH"], 2, 5, 5000, 20000, [Season.SUMMER], "MODERATE")
    }

    private DestinationSummary buildDestinationSummary(String id) {
        new DestinationSummary(id, id, id.capitalize(), "Some State", "LADAKH", [], [])
    }

    private RecommendationResponse buildRecommendationResponse(String destId) {
        def dest = new DestinationSummaryDto(destId, destId, destId.capitalize(), "State", "Region", [], [])
        new RecommendationResponse(dest, 80.0, MatchLevel.VERY_GOOD, ["Some reason"], [], [], [])
    }
}
