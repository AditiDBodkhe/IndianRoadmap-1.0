package com.indianroadmap.gateway.ratelimit;

import java.time.Duration;

public interface RateLimiter {
    boolean allow(String key, int limit, Duration window);
}
