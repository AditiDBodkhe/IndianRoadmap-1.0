package com.indianroadmap.user.service

import com.indianroadmap.user.config.JwtProperties
import com.indianroadmap.user.document.UserRole
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class JwtServiceSpec extends Specification {

    JwtProperties props = new JwtProperties("test-secret-key-must-be-32-chars-minimum", 900, 2592000)
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
    JwtService jwtService = new JwtService(props, clock)

    def "generateAccessToken produces valid JWT"() {
        when:
        def token = jwtService.generateAccessToken("user-1", UserRole.USER)

        then:
        token
        jwtService.validateToken(token)
    }

    def "extractUserId returns correct subject"() {
        given:
        def token = jwtService.generateAccessToken("user-123", UserRole.ADMIN)

        expect:
        jwtService.extractUserId(token) == "user-123"
    }

    def "extractRole returns correct role"() {
        given:
        def token = jwtService.generateAccessToken("user-123", UserRole.CONTENT_EDITOR)

        expect:
        jwtService.extractRole(token) == UserRole.CONTENT_EDITOR
    }

    def "isExpired returns false for fresh token"() {
        given:
        def token = jwtService.generateAccessToken("user-1", UserRole.USER)

        expect:
        !jwtService.isExpired(token)
    }

    def "validateToken returns false for tampered token"() {
        given:
        def token = jwtService.generateAccessToken("user-1", UserRole.USER)

        expect:
        !jwtService.validateToken(token + "tampered")
    }

    def "validateToken returns false for expired token"() {
        given:
        def shortProps = new JwtProperties("test-secret-key-must-be-32-chars-minimum", 1, 2592000)
        def baseClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        def tokenService = new JwtService(shortProps, baseClock)
        def laterService = new JwtService(shortProps, Clock.offset(baseClock, Duration.ofSeconds(2)))
        def token = tokenService.generateAccessToken("user-1", UserRole.USER)

        expect:
        !laterService.validateToken(token)
    }
}
