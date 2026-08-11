package com.indianroadmap.gateway

import com.indianroadmap.gateway.ratelimit.InMemoryRateLimiter
import com.indianroadmap.gateway.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification

@SpringBootTest
@ActiveProfiles('test')
class GatewayRoutingSpec extends Specification {

    @Autowired
    ApplicationContext context

    @LocalServerPort
    int port

    WebTestClient webTestClient

    def setup() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
    }

    def 'application context loads'() {
        expect:
        context != null
    }

    def 'JwtService bean exists'() {
        expect:
        context.getBean(JwtService) != null
    }

    def 'InMemoryRateLimiter bean exists'() {
        expect:
        context.getBean(InMemoryRateLimiter) != null
    }

    def 'health endpoint works'() {
        expect:
        webTestClient.get()
                .uri('/actuator/health')
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath('$.status').isEqualTo('UP')
    }

    def 'protected user endpoint requires authentication'() {
        expect:
        webTestClient.get()
                .uri('/api/v1/users/me')
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath('$.error.code').isEqualTo('UNAUTHORIZED')
    }

    def 'public auth endpoint passes through gateway when downstream unavailable'() {
        expect:
        webTestClient.post()
                .uri('/api/v1/auth/login')
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath('$.error.code').isEqualTo('SERVICE_UNAVAILABLE')
    }

    def 'public ai endpoint does not require authentication'() {
        when:
        def response = webTestClient.post()
                .uri('/api/ai/recommendations/mood')
                .bodyValue([mood: 'SPIRITUAL'])
                .exchange()
                .returnResult(String)

        then:
        response.status != HttpStatus.UNAUTHORIZED
    }
}
