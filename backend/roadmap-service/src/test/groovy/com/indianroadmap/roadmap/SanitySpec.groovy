package com.indianroadmap.roadmap

import com.indianroadmap.roadmap.document.RoadmapDocument
import com.indianroadmap.roadmap.document.RouteSummaryDocument
import spock.lang.Specification

class SanitySpec extends Specification {

    def "basic arithmetic works"() {
        expect:
        1 + 1 == 2
    }

    def "roadmap document initializes collections"() {
        when:
        def document = new RoadmapDocument()

        then:
        document.nodes != null
        document.edges != null
        document.routeSummary != null
    }

    def "route summary defaults to zeros"() {
        given:
        def summary = new RouteSummaryDocument()

        expect:
        summary.totalDistanceKm == 0.0d
        summary.totalTravelTimeMinutes == 0
    }
}
