package com.indianroadmap.gateway.filter;

import com.indianroadmap.gateway.security.GatewayUser;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-100)
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange sanitizedExchange = mutateExchange(exchange, null);
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(authentication -> mutateExchange(exchange, extractGatewayUser(authentication)))
                .defaultIfEmpty(sanitizedExchange)
                .flatMap(chain::filter);
    }

    private ServerWebExchange mutateExchange(ServerWebExchange exchange, GatewayUser gatewayUser) {
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USER_ROLE_HEADER);
                    if (gatewayUser != null) {
                        headers.set(USER_ID_HEADER, gatewayUser.userId());
                        headers.set(USER_ROLE_HEADER, gatewayUser.normalizedRole());
                    }
                }))
                .build();
    }

    private GatewayUser extractGatewayUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof GatewayUser gatewayUser)) {
            return null;
        }
        return gatewayUser;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
