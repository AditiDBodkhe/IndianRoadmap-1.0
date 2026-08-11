package com.indianroadmap.user.service

import com.indianroadmap.user.document.BudgetRange
import com.indianroadmap.user.document.UserDocument
import com.indianroadmap.user.document.UserPreferences
import com.indianroadmap.user.dto.request.UpdatePreferencesRequest
import com.indianroadmap.user.dto.request.UpdateProfileRequest
import com.indianroadmap.user.exception.UserNotFoundException
import com.indianroadmap.user.repository.UserRepository
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class UserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
    UserService userService = new UserServiceImpl(userRepository, clock)

    def "getCurrentUser returns mapped user response"() {
        given:
        def user = sampleUser()
        userRepository.findById("user-1") >> Optional.of(user)

        when:
        def response = userService.getCurrentUser("user-1")

        then:
        response.id() == "user-1"
        response.email() == "user@example.com"
    }

    def "updateCurrentUser updates mutable profile fields"() {
        given:
        def user = sampleUser()
        userRepository.findById("user-1") >> Optional.of(user)
        userRepository.save(_ as UserDocument) >> { UserDocument doc -> doc }

        when:
        def response = userService.updateCurrentUser("user-1", new UpdateProfileRequest("Updated", "User", "Display",
                "Bio", "https://img", "en"))

        then:
        response.firstName() == "Updated"
        response.displayName() == "Display"
        user.updatedAt == Instant.parse("2026-01-01T00:00:00Z")
    }

    def "getPreferences returns empty response when preferences missing"() {
        given:
        def user = sampleUser()
        user.preferences = null
        userRepository.findById("user-1") >> Optional.of(user)

        expect:
        userService.getPreferences("user-1").preferredMoods().isEmpty()
    }

    def "updatePreferences replaces preferences with immutable copies"() {
        given:
        def user = sampleUser()
        userRepository.findById("user-1") >> Optional.of(user)
        userRepository.save(_ as UserDocument) >> { UserDocument doc -> doc }

        when:
        def response = userService.updatePreferences("user-1", new UpdatePreferencesRequest(
                ["adventure"], ["culture"], ["road-trip"], ["north"], ["en"], 7, new BudgetRange(1000, 5000)))

        then:
        response.preferredMoods() == ["adventure"]
        user.preferences.preferredRegions == ["north"]
    }

    def "getCurrentUser throws UserNotFoundException when missing"() {
        given:
        userRepository.findById("missing") >> Optional.empty()

        when:
        userService.getCurrentUser("missing")

        then:
        thrown(UserNotFoundException)
    }

    private static UserDocument sampleUser() {
        def user = new UserDocument()
        user.id = "user-1"
        user.email = "user@example.com"
        user.firstName = "Aditi"
        user.lastName = "Bodkhe"
        user.displayName = "Aditi"
        user.preferences = new UserPreferences(["relaxed"], ["food"], ["solo"], ["west"], ["en"], 5, new BudgetRange(500, 2500))
        user
    }
}
