package com.indianroadmap.destination

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

@Title("Sanity — Groovy + Spock framework verification")
class SanitySpec extends Specification {

    def "Groovy and Spock are on the classpath and working"() {
        expect:
        1 + 1 == 2
    }

    def "String records are immutable by nature in Groovy"() {
        given:
        def slug = "chhitkul"

        when:
        def upper = slug.toUpperCase()

        then:
        slug   == "chhitkul"
        upper  == "CHHITKUL"
    }

    def "Collections are handled with Groovy idioms"() {
        given:
        def moods = ["ADVENTURE", "ZEN", "SOLITUDE"]

        when:
        def sorted = moods.sort(false)

        then:
        sorted == ["ADVENTURE", "SOLITUDE", "ZEN"]
        moods  == ["ADVENTURE", "ZEN", "SOLITUDE"]   // original unchanged
    }

    def "Data-driven: slug format validation logic"() {
        expect:
        slug ==~ /^[a-z0-9]+(-[a-z0-9]+)*$/

        where:
        slug           | _
        "chhitkul"     | _
        "adi-kailash"  | _
        "tabo"         | _
        "hanle"        | _
        "turtuk"       | _
    }
}
