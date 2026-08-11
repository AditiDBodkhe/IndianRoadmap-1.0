package com.indianroadmap.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

        return chain.filter(exchange).doFinally(signalType -> {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
            int status = statusCode != null ? statusCode.value() : 0;
            log.info("correlationId={} method={} path={} status={} durationMs={}", correlationId, method, path, status, durationMs);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
