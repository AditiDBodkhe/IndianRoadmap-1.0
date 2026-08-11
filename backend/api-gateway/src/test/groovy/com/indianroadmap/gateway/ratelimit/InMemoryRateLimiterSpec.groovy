package com.indianroadmap.gateway.ratelimit

import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class InMemoryRateLimiterSpec extends Specification {

    MutableClock clock = new MutableClock(Instant.parse('2026-01-01T00:00:00Z'))
    InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter(clock)

    def 'allows requests within limit'() {
        expect:
        rateLimiter.allow('client-a', 3, Duration.ofMinutes(1))
        rateLimiter.allow('client-a', 3, Duration.ofMinutes(1))
        rateLimiter.allow('client-a', 3, Duration.ofMinutes(1))
    }

    def 'blocks requests exceeding limit'() {
        given:
        3.times { rateLimiter.allow('client-a', 3, Duration.ofMinutes(1)) }

        expect:
        !rateLimiter.allow('client-a', 3, Duration.ofMinutes(1))
    }

    def 'sliding window resets after window expires'() {
        given:
        2.times { rateLimiter.allow('client-a', 2, Duration.ofSeconds(30)) }
        clock.advance(Duration.ofSeconds(31))

        expect:
        rateLimiter.allow('client-a', 2, Duration.ofSeconds(30))
        rateLimiter.allow('client-a', 2, Duration.ofSeconds(30))
        !rateLimiter.allow('client-a', 2, Duration.ofSeconds(30))
    }

    def 'different keys are independent'() {
        given:
        2.times { rateLimiter.allow('client-a', 2, Duration.ofMinutes(1)) }

        expect:
        !rateLimiter.allow('client-a', 2, Duration.ofMinutes(1))
        rateLimiter.allow('client-b', 2, Duration.ofMinutes(1))
    }

    private static final class MutableClock extends Clock {
        private Instant current

        private MutableClock(Instant current) {
            this.current = current
        }

        @Override
        ZoneId getZone() {
            return ZoneOffset.UTC
        }

        @Override
        Clock withZone(ZoneId zone) {
            return this
        }

        @Override
        Instant instant() {
            return current
        }

        void advance(Duration duration) {
            current = current.plus(duration)
        }
    }
}
