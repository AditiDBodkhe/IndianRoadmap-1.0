package com.indianroadmap.gateway.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InMemoryRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean allow(String key, int limit, Duration window) {
        Instant now = Instant.now(clock);
        Instant windowStart = now.minus(window);
        AtomicBoolean allowed = new AtomicBoolean(false);

        requests.compute(key, (ignored, deque) -> {
            Deque<Instant> requestTimes = deque == null ? new ArrayDeque<>() : deque;
            while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(windowStart)) {
                requestTimes.pollFirst();
            }
            if (requestTimes.size() < limit) {
                requestTimes.addLast(now);
                allowed.set(true);
            }
            return requestTimes;
        });

        return allowed.get();
    }

    public void cleanUp(Duration maxAge) {
        Instant threshold = Instant.now(clock).minus(maxAge);
        requests.entrySet().removeIf(entry -> {
            Deque<Instant> deque = entry.getValue();
            while (!deque.isEmpty() && deque.peekFirst().isBefore(threshold)) {
                deque.pollFirst();
            }
            return deque.isEmpty();
        });
    }
}
