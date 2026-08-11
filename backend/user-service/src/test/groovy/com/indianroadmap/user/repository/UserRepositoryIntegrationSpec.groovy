package com.indianroadmap.user.repository

import com.indianroadmap.user.document.AccountStatus
import com.indianroadmap.user.document.UserDocument
import com.indianroadmap.user.document.UserPreferences
import com.indianroadmap.user.document.UserRole
import org.junit.jupiter.api.Assumptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.mongodb.MongoDBContainer
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserRepositoryIntegrationSpec extends Specification {

    @Shared
    static MongoDBContainer mongo

    static {
        try {
            mongo = new MongoDBContainer("mongo:8.0")
            mongo.start()
        } catch (Throwable ignored) {
            mongo = null
        }
    }

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        if (mongo != null) {
            registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl)
        }
    }

    @Autowired
    UserRepository repository

    def setupSpec() {
        Assumptions.assumeTrue(mongo != null, "Docker not available — skipping integration tests")
    }

    def setup() {
        repository.deleteAll()
    }

    def "saves and retrieves a user by email"() {
        given:
        repository.save(sampleUser("user@example.com"))

        when:
        def result = repository.findByEmail("user@example.com")

        then:
        result.present
        result.get().email == "user@example.com"
    }

    def "existsByEmail returns true for stored user"() {
        given:
        repository.save(sampleUser("exists@example.com"))

        expect:
        repository.existsByEmail("exists@example.com")
    }

    def "enforces unique email constraint"() {
        given:
        repository.save(sampleUser("dup@example.com"))

        when:
        repository.save(sampleUser("dup@example.com"))

        then:
        thrown(Exception)
    }

    private static UserDocument sampleUser(String email) {
        def user = new UserDocument()
        user.email = email
        user.passwordHash = "hash"
        user.firstName = "Aditi"
        user.lastName = "Bodkhe"
        user.displayName = "Aditi"
        user.role = UserRole.USER
        user.status = AccountStatus.ACTIVE
        user.preferences = new UserPreferences()
        user.createdAt = Instant.parse("2026-01-01T00:00:00Z")
        user.updatedAt = Instant.parse("2026-01-01T00:00:00Z")
        user
    }
}
