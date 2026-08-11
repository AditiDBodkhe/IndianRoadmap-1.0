package com.indianroadmap.user.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

class PasswordServiceSpec extends Specification {

    PasswordService service = new PasswordService(new BCryptPasswordEncoder())

    def "hashPassword produces different hash each time"() {
        when:
        def first = service.hashPassword("super-secret-pass")
        def second = service.hashPassword("super-secret-pass")

        then:
        first != second
    }

    def "matches returns true for correct password"() {
        given:
        def hash = service.hashPassword("super-secret-pass")

        expect:
        service.matches("super-secret-pass", hash)
    }

    def "matches returns false for wrong password"() {
        given:
        def hash = service.hashPassword("super-secret-pass")

        expect:
        !service.matches("wrong-pass", hash)
    }

    def "hash does not contain plaintext password"() {
        when:
        def hash = service.hashPassword("super-secret-pass")

        then:
        !hash.contains("super-secret-pass")
    }
}
