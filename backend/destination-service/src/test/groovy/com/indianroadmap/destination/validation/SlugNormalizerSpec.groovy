package com.indianroadmap.destination.validation

import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("SlugNormalizer — normalization and validation rules")
class SlugNormalizerSpec extends Specification {

    SlugNormalizer slugNormalizer = new SlugNormalizer()

    @Unroll
    def "normalizes '#input' to '#expected'"() {
        expect:
        slugNormalizer.normalize(input) == expected

        where:
        input             | expected
        "Chhitkul"        | "chhitkul"
        "Adi Kailash"     | "adi-kailash"
        "  Hanle  "       | "hanle"
        "Tabo"            | "tabo"
        "Mana village"    | "mana-village"
        "KAZA"            | "kaza"
        "Adi   Kailash"   | "adi-kailash"
        "turtuk"          | "turtuk"
        ""                | ""
    }

    @Unroll
    def "validates slug '#slug' as '#valid'"() {
        expect:
        slugNormalizer.isValid(slug) == valid

        where:
        slug          | valid
        "chhitkul"    | true
        "adi-kailash" | true
        "mana"        | true
        ""            | false
        "Chhitkul"    | false
        "adi kailash" | false
        "-invalid"    | false
        "invalid-"    | false
        null          | false
    }
}
