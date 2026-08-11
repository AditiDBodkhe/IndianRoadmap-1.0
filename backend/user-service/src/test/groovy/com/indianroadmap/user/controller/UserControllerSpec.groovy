package com.indianroadmap.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.indianroadmap.user.document.AccountStatus
import com.indianroadmap.user.document.BudgetRange
import com.indianroadmap.user.document.UserRole
import com.indianroadmap.user.dto.request.UpdatePreferencesRequest
import com.indianroadmap.user.dto.request.UpdateProfileRequest
import com.indianroadmap.user.dto.response.UserPreferencesResponse
import com.indianroadmap.user.dto.response.UserResponse
import com.indianroadmap.user.exception.GlobalExceptionHandler
import com.indianroadmap.user.service.JwtService
import com.indianroadmap.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.when
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [UserController])
@Import([GlobalExceptionHandler])
class UserControllerSpec extends Specification {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    UserService userService

    @MockitoBean
    JwtService jwtService

    def setup() {
        when(jwtService.validateToken("token")).thenReturn(true)
        when(jwtService.extractUserId("token")).thenReturn("user-1")
        when(jwtService.extractRole("token")).thenReturn(UserRole.USER)
    }

    def "GET /me returns 200"() {
        given:
        when(userService.getCurrentUser(any())).thenReturn(sampleUser())

        expect:
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.id').value('user-1'))
    }

    def "PUT /me returns 200"() {
        given:
        when(userService.updateCurrentUser(eq("user-1"), any())).thenReturn(sampleUser())
        def body = MAPPER.writeValueAsString(new UpdateProfileRequest("Aditi", "Bodkhe", "Aditi", "Bio", null, "en"))

        expect:
        mockMvc.perform(put("/api/v1/users/me")
                .header("Authorization", "Bearer token")
                .with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "GET /me/preferences returns 200"() {
        given:
        when(userService.getPreferences(any())).thenReturn(new UserPreferencesResponse(["adventure"], ["culture"],
                ["road-trip"], ["north"], ["en"], 7, new BudgetRange(1000, 5000)))

        expect:
        mockMvc.perform(get("/api/v1/users/me/preferences")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.preferredMoods[0]').value('adventure'))
    }

    def "PUT /me/preferences returns 200"() {
        given:
        when(userService.updatePreferences(any(), any())).thenReturn(new UserPreferencesResponse(["adventure"], ["culture"],
                ["road-trip"], ["north"], ["en"], 7, new BudgetRange(1000, 5000)))
        def body = MAPPER.writeValueAsString(new UpdatePreferencesRequest(["adventure"], ["culture"],
                ["road-trip"], ["north"], ["en"], 7, new BudgetRange(1000, 5000)))

        expect:
        mockMvc.perform(put("/api/v1/users/me/preferences")
                .header("Authorization", "Bearer token")
                .with(csrf())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.preferredRegions[0]').value('north'))
    }

    private static UserResponse sampleUser() {
        new UserResponse("user-1", "user@example.com", "Aditi", "Bodkhe", "Aditi", null, null,
                UserRole.USER, AccountStatus.ACTIVE, Instant.parse("2026-01-01T00:00:00Z"), null)
    }
}
