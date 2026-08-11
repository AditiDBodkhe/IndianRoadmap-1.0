package com.indianroadmap.gateway.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public record GatewayUser(String userId, String role) implements Principal {

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole()));
    }

    public String normalizedRole() {
        return role == null || role.isBlank() ? "USER" : role.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String getName() {
        return userId;
    }
}
