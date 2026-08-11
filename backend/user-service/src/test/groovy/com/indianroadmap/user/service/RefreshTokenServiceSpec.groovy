package com.indianroadmap.user.service

import com.indianroadmap.user.config.JwtProperties
import com.indianroadmap.user.document.RefreshTokenDocument
import com.indianroadmap.user.exception.RefreshTokenExpiredException
import com.indianroadmap.user.exception.RefreshTokenInvalidException
import com.indianroadmap.user.repository.RefreshTokenRepository
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RefreshTokenServiceSpec extends Specification {

    RefreshTokenRepository repository = Mock()
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
    JwtProperties props = new JwtProperties("test-secret-key-must-be-32-chars-minimum", 900, 2592000)
    JwtService jwtService = new JwtService(props, clock)
    RefreshTokenService service = new RefreshTokenService(repository, clock, props, jwtService)

    def "createRefreshToken stores hashed token"() {
        given:
        RefreshTokenDocument savedDoc = null

        when:
        def rawToken = service.createRefreshToken("user-1")

        then:
        rawToken
        1 * repository.save(_ as RefreshTokenDocument) >> { RefreshTokenDocument doc ->
            savedDoc = doc
            doc
        }
        savedDoc.userId == "user-1"
        savedDoc.tokenHash == service.hashToken(rawToken)
        savedDoc.expiresAt == Instant.parse("2026-01-31T00:00:00Z")
    }

    def "validateAndRotate revokes old token and creates new token"() {
        given:
        def doc = new RefreshTokenDocument()
        doc.userId = "user-1"
        doc.tokenHash = service.hashToken("old-token")
        doc.expiresAt = Instant.parse("2026-01-02T00:00:00Z")
        repository.findByTokenHash(service.hashToken("old-token")) >> Optional.of(doc)

        when:
        def newToken = service.validateAndRotate("old-token")

        then:
        newToken
        2 * repository.save(_ as RefreshTokenDocument)
        doc.revokedAt == Instant.parse("2026-01-01T00:00:00Z")
    }

    def "revokeToken marks token revoked"() {
        given:
        def doc = new RefreshTokenDocument()
        doc.tokenHash = service.hashToken("raw-token")
        repository.findByTokenHash(service.hashToken("raw-token")) >> Optional.of(doc)

        when:
        service.revokeToken("raw-token")

        then:
        1 * repository.save({ RefreshTokenDocument saved -> saved.revokedAt == Instant.parse("2026-01-01T00:00:00Z") })
    }

    def "validateAndRotate throws RefreshTokenExpiredException for expired token"() {
        given:
        def doc = new RefreshTokenDocument()
        doc.tokenHash = service.hashToken("expired-token")
        doc.expiresAt = Instant.parse("2025-12-31T23:59:59Z")
        repository.findByTokenHash(service.hashToken("expired-token")) >> Optional.of(doc)

        when:
        service.validateAndRotate("expired-token")

        then:
        thrown(RefreshTokenExpiredException)
    }

    def "requireValidToken throws RefreshTokenInvalidException for missing token"() {
        given:
        repository.findByTokenHash(service.hashToken("missing-token")) >> Optional.empty()

        when:
        service.requireValidToken("missing-token")

        then:
        thrown(RefreshTokenInvalidException)
    }
}
