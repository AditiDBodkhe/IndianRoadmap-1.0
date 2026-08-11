package com.indianroadmap.gateway.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.test.StepVerifier
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference

class CorrelationIdFilterSpec extends Specification {

    CorrelationIdFilter filter = new CorrelationIdFilter()

    def 'generates UUID when no correlation ID present'() {
        given:
        def exchange = MockServerWebExchange.from(MockServerHttpRequest.get('/test').build())
        def capturedExchange = new AtomicReference<org.springframework.web.server.ServerWebExchange>()
        GatewayFilterChain chain = Mock() {
            1 * filter(_ as org.springframework.web.server.ServerWebExchange) >> { org.springframework.web.server.ServerWebExchange mutated ->
                capturedExchange.set(mutated)
                mutated.response.setComplete()
            }
        }

        when:
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()
        def header = capturedExchange.get().request.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER)

        then:
        header != null
        header ==~ /[0-9a-f\-]{36}/
        exchange.response.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER) == header
    }

    def 'propagates existing correlation ID'() {
        given:
        def exchange = MockServerWebExchange.from(MockServerHttpRequest.get('/test')
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, 'request-123')
                .build())
        def capturedExchange = new AtomicReference<org.springframework.web.server.ServerWebExchange>()
        GatewayFilterChain chain = Mock() {
            1 * filter(_ as org.springframework.web.server.ServerWebExchange) >> { org.springframework.web.server.ServerWebExchange mutated ->
                capturedExchange.set(mutated)
                mutated.response.setComplete()
            }
        }

        when:
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        then:
        capturedExchange.get().request.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER) == 'request-123'
        exchange.response.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER) == 'request-123'
    }

    def 'sanitizes malicious correlation ID'() {
        given:
        def exchange = MockServerWebExchange.from(MockServerHttpRequest.get('/test')
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, 'abc<script>-_123')
                .build())
        def capturedExchange = new AtomicReference<org.springframework.web.server.ServerWebExchange>()
        GatewayFilterChain chain = Mock() {
            1 * filter(_ as org.springframework.web.server.ServerWebExchange) >> { org.springframework.web.server.ServerWebExchange mutated ->
                capturedExchange.set(mutated)
                mutated.response.setComplete()
            }
        }

        when:
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete()

        then:
        capturedExchange.get().request.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER) == 'abcscript-_123'
        exchange.response.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER) == 'abcscript-_123'
    }
}
