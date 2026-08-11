package com.indianroadmap.destination.validation

import com.indianroadmap.destination.exception.InvalidDestinationException
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("DestinationValidator — coordinate and elevation validation")
class DestinationValidatorSpec extends Specification {

    DestinationValidator validator = new DestinationValidator()

    @Unroll
    def "rejects invalid latitude #lat"() {
        when:
        validator.validateCoordinates(lat, 0.0)

        then:
        thrown(InvalidDestinationException)

        where:
        lat << [-91.0, 91.0, -180.0, 200.0]
    }

    @Unroll
    def "rejects invalid longitude #lon"() {
        when:
        validator.validateCoordinates(0.0, lon)

        then:
        thrown(InvalidDestinationException)

        where:
        lon << [-181.0, 181.0, 360.0, -360.0]
    }

    def "accepts valid Indian coordinates"() {
        expect:
        validator.validateCoordinates(31.35, 78.44)
        true
    }

    def "rejects negative elevation"() {
        when:
        validator.validateElevation(-1)

        then:
        thrown(InvalidDestinationException)
    }

    def "accepts zero elevation"() {
        expect:
        validator.validateElevation(0)
        true
    }

    def "rejects zero radius"() {
        when:
        validator.validateNearbySearchRadius(0)

        then:
        thrown(InvalidDestinationException)
    }

    def "rejects negative radius"() {
        when:
        validator.validateNearbySearchRadius(-100)

        then:
        thrown(InvalidDestinationException)
    }
}
