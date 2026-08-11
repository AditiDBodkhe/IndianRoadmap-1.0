package com.indianroadmap.user.service

import com.indianroadmap.user.document.AccountStatus
import com.indianroadmap.user.document.RefreshTokenDocument
import com.indianroadmap.user.document.UserDocument
import com.indianroadmap.user.document.UserRole
import com.indianroadmap.user.dto.request.LoginRequest
import com.indianroadmap.user.dto.request.LogoutRequest
import com.indianroadmap.user.dto.request.RefreshTokenRequest
import com.indianroadmap.user.dto.request.RegisterRequest
import com.indianroadmap.user.exception.AccountDisabledException
import com.indianroadmap.user.exception.AccountLockedException
import com.indianroadmap.user.exception.InvalidCredentialsException
import com.indianroadmap.user.exception.UserAlreadyExistsException
import com.indianroadmap.user.repository.UserRepository
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuthServiceSpec extends Specification {

    UserRepository userRepo = Mock()
    PasswordService passwordService = Mock()
    JwtService jwtService = Mock()
    RefreshTokenService refreshTokenService = Mock()
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    AuthService authService = new AuthServiceImpl(userRepo, passwordService, jwtService, refreshTokenService, clock)

    def "register creates user with hashed password"() {
        given:
        def request = new RegisterRequest(" Test@Example.com ", "super-secure-pass", "Aditi", "Bodkhe", null)
        userRepo.existsByEmail("test@example.com") >> false
        passwordService.hashPassword("super-secure-pass") >> "hashed"
        userRepo.save(_ as UserDocument) >> { UserDocument doc ->
            doc.setId("user-1")
            doc
        }

        when:
        def response = authService.register(request)

        then:
        response.id() == "user-1"
        response.email() == "test@example.com"
        response.displayName() == "Aditi Bodkhe"
    }

    def "register throws UserAlreadyExistsException for duplicate email"() {
        given:
        userRepo.existsByEmail("dup@example.com") >> true

        when:
        authService.register(new RegisterRequest("dup@example.com", "super-secure-pass", "A", "B", null))

        then:
        thrown(UserAlreadyExistsException)
    }

    def "login returns tokens for valid credentials"() {
        given:
        def user = activeUser("user-1")
        userRepo.findByEmail("user@example.com") >> Optional.of(user)
        passwordService.matches("password", "hash") >> true
        jwtService.generateAccessToken("user-1", UserRole.USER) >> "access-token"
        jwtService.getAccessTokenExpirationSeconds() >> 900L
        refreshTokenService.createRefreshToken("user-1") >> "refresh-token"
        userRepo.save(_ as UserDocument) >> { UserDocument doc -> doc }

        when:
        def response = authService.login(new LoginRequest("user@example.com", "password"))

        then:
        response.accessToken() == "access-token"
        response.refreshToken() == "refresh-token"
        user.lastLoginAt == Instant.parse("2026-01-01T00:00:00Z")
    }

    def "login throws InvalidCredentialsException for wrong password"() {
        given:
        userRepo.findByEmail("user@example.com") >> Optional.of(activeUser("user-1"))
        passwordService.matches("wrong", "hash") >> false

        when:
        authService.login(new LoginRequest("user@example.com", "wrong"))

        then:
        thrown(InvalidCredentialsException)
    }

    def "login throws InvalidCredentialsException for unknown email"() {
        given:
        userRepo.findByEmail("unknown@example.com") >> Optional.empty()

        when:
        authService.login(new LoginRequest("unknown@example.com", "password"))

        then:
        thrown(InvalidCredentialsException)
    }

    def "login throws AccountLockedException for locked user"() {
        given:
        def user = activeUser("user-1")
        user.status = AccountStatus.LOCKED
        userRepo.findByEmail("user@example.com") >> Optional.of(user)
        passwordService.matches("password", "hash") >> true

        when:
        authService.login(new LoginRequest("user@example.com", "password"))

        then:
        thrown(AccountLockedException)
    }

    def "login throws AccountDisabledException for disabled user"() {
        given:
        def user = activeUser("user-1")
        user.status = AccountStatus.DISABLED
        userRepo.findByEmail("user@example.com") >> Optional.of(user)
        passwordService.matches("password", "hash") >> true

        when:
        authService.login(new LoginRequest("user@example.com", "password"))

        then:
        thrown(AccountDisabledException)
    }

    def "refresh rotates tokens"() {
        given:
        def refreshToken = new RefreshTokenDocument()
        refreshToken.userId = "user-1"
        refreshToken.tokenHash = "hash"
        refreshToken.expiresAt = Instant.parse("2026-02-01T00:00:00Z")
        def user = activeUser("user-1")
        userRepo.findById("user-1") >> Optional.of(user)
        refreshTokenService.requireValidToken("old-token") >> refreshToken
        refreshTokenService.rotateToken(refreshToken) >> "new-refresh"
        jwtService.generateAccessToken("user-1", UserRole.USER) >> "new-access"
        jwtService.getAccessTokenExpirationSeconds() >> 900L

        when:
        def response = authService.refresh(new RefreshTokenRequest("old-token"))

        then:
        response.accessToken() == "new-access"
        response.refreshToken() == "new-refresh"
    }

    def "logout revokes refresh token"() {
        when:
        authService.logout(new LogoutRequest("refresh-token"))

        then:
        1 * refreshTokenService.revokeToken("refresh-token")
    }

    private static UserDocument activeUser(String id) {
        def user = new UserDocument()
        user.id = id
        user.email = "user@example.com"
        user.passwordHash = "hash"
        user.role = UserRole.USER
        user.status = AccountStatus.ACTIVE
        user
    }
}
