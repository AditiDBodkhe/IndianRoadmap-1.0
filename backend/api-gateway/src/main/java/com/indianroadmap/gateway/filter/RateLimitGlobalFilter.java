package com.indianroadmap.gateway.filter;

import com.indianroadmap.gateway.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

@Component
@Order(-175)
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );
    private static final int REQUEST_LIMIT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final RateLimiter rateLimiter;

    public RateLimitGlobalFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() != HttpMethod.POST || !AUTH_PATHS.contains(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        String key = resolveClientKey(exchange);
        if (rateLimiter.allow(key, REQUEST_LIMIT, WINDOW)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(WINDOW.toSeconds()));
        byte[] body = "{\"success\":false,\"data\":null,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"Too many authentication requests\"}}".getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown-client";
    }

    @Override
    public int getOrder() {
        return -175;
    }
}
