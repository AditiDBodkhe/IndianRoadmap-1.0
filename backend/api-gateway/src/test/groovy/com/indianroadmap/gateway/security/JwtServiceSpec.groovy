package com.indianroadmap.gateway.security

import com.indianroadmap.gateway.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import spock.lang.Specification
import spock.lang.Subject

import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

class JwtServiceSpec extends Specification {

    private static final String SECRET = 'test-secret-key-that-is-at-least-32-characters-long'

    @Subject
    JwtService jwtService = new JwtService(new JwtProperties(SECRET))

    def 'valid token validates successfully'() {
        given:
        def token = generateToken(SECRET, 'user-1', 'USER')

        expect:
        jwtService.validateToken(token)
        !jwtService.isExpired(token)
    }

    def 'expired token returns false'() {
        given:
        def token = generateToken(SECRET, 'user-1', 'USER', -60)

        expect:
        !jwtService.validateToken(token)
        jwtService.isExpired(token)
    }

    def 'tampered token returns false'() {
        given:
        def token = generateToken(SECRET, 'user-1', 'USER') + 'tampered'

        expect:
        !jwtService.validateToken(token)
        jwtService.isExpired(token)
    }

    def 'extractUserId returns correct subject'() {
        given:
        def token = generateToken(SECRET, 'user-42', 'USER')

        expect:
        jwtService.extractUserId(token) == 'user-42'
    }

    def 'extractRole returns correct role'() {
        given:
        def token = generateToken(SECRET, 'user-42', 'ADMIN')

        expect:
        jwtService.extractRole(token) == 'ADMIN'
    }

    def 'token with USER role'() {
        given:
        def token = generateToken(SECRET, 'traveler-1', 'USER')

        expect:
        jwtService.validateToken(token)
        jwtService.extractRole(token) == 'USER'
    }

    def 'token with ADMIN role'() {
        given:
        def token = generateToken(SECRET, 'admin-1', 'ADMIN')

        expect:
        jwtService.validateToken(token)
        jwtService.extractRole(token) == 'ADMIN'
    }

    private static String generateToken(String secret, String userId, String role, long expirySeconds = 900) {
        def key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))
        return Jwts.builder()
                .subject(userId)
                .claim('role', role)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expirySeconds)))
                .signWith(key)
                .compact()
    }
}
