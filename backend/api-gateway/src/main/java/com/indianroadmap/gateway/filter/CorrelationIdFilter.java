package com.indianroadmap.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(-200)
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final int MAX_CORRELATION_ID_LENGTH = 64;
    private static final Pattern UNSAFE_CHARACTERS = Pattern.compile("[^a-zA-Z0-9\\-_]");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = sanitizeCorrelationId(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER));
        if (correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, finalCorrelationId);
            return Mono.empty();
        });

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(CORRELATION_ID_HEADER, finalCorrelationId)))
                .build();

        return chain.filter(mutatedExchange);
    }

    String sanitizeCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return "";
        }
        String sanitized = UNSAFE_CHARACTERS.matcher(correlationId).replaceAll("");
        if (sanitized.length() > MAX_CORRELATION_ID_LENGTH) {
            sanitized = sanitized.substring(0, MAX_CORRELATION_ID_LENGTH);
        }
        return sanitized;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
