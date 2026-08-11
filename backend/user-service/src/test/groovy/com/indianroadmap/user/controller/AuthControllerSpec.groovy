package com.indianroadmap.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.indianroadmap.user.dto.request.LoginRequest
import com.indianroadmap.user.dto.request.LogoutRequest
import com.indianroadmap.user.dto.request.RefreshTokenRequest
import com.indianroadmap.user.dto.request.RegisterRequest
import com.indianroadmap.user.dto.response.AuthResponse
import com.indianroadmap.user.dto.response.UserResponse
import com.indianroadmap.user.exception.GlobalExceptionHandler
import com.indianroadmap.user.exception.InvalidCredentialsException
import com.indianroadmap.user.service.AuthService
import com.indianroadmap.user.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.Instant

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AuthController], excludeAutoConfiguration = [SecurityAutoConfiguration, UserDetailsServiceAutoConfiguration])
@Import([GlobalExceptionHandler])
class AuthControllerSpec extends Specification {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Autowired
    MockMvc mockMvc

    @MockitoBean
    AuthService authService

    @MockitoBean
    JwtService jwtService

    def "POST /register returns 201"() {
        given:
        when(authService.register(any())).thenReturn(new UserResponse("user-1", "user@example.com", "Aditi", "Bodkhe",
                "Aditi", null, null, null, null, Instant.parse("2026-01-01T00:00:00Z"), null))
        def body = MAPPER.writeValueAsString(new RegisterRequest("user@example.com", "super-secret-pass", "Aditi", "Bodkhe", "Aditi"))

        expect:
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
    }

    def "POST /register with invalid email returns 400"() {
        expect:
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"email":"bad","password":"super-secret-pass","firstName":"A","lastName":"B"}'))
                .andExpect(status().isBadRequest())
    }

    def "POST /login returns 200 with tokens"() {
        given:
        when(authService.login(any())).thenReturn(AuthResponse.of("access", "refresh", 900))
        def body = MAPPER.writeValueAsString(new LoginRequest("user@example.com", "password"))

        expect:
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.accessToken').value('access'))
    }

    def "POST /login with wrong credentials returns 401"() {
        given:
        when(authService.login(any())).thenThrow(new InvalidCredentialsException())
        def body = MAPPER.writeValueAsString(new LoginRequest("user@example.com", "password"))

        expect:
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath('$.error.code').value('INVALID_CREDENTIALS'))
    }

    def "POST /refresh returns 200"() {
        given:
        when(authService.refresh(any())).thenReturn(AuthResponse.of("access", "refresh-2", 900))
        def body = MAPPER.writeValueAsString(new RefreshTokenRequest("refresh"))

        expect:
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.data.refreshToken').value('refresh-2'))
    }

    def "POST /logout returns 204"() {
        given:
        def body = MAPPER.writeValueAsString(new LogoutRequest("refresh"))

        expect:
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNoContent())
    }
}
