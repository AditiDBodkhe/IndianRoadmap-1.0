package com.indianroadmap.gateway.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            return Mono.empty();
        }

        String token = String.valueOf(authentication.getCredentials());
        if (!jwtService.validateToken(token)) {
            return Mono.error(new BadCredentialsException("JWT is invalid or expired"));
        }

        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);
        GatewayUser principal = new GatewayUser(userId, role);

        return Mono.just(UsernamePasswordAuthenticationToken.authenticated(principal, token, principal.authorities()));
    }
}
