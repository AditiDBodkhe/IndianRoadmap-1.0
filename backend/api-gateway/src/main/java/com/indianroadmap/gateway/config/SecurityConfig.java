package com.indianroadmap.gateway.config;

import com.indianroadmap.gateway.security.JwtAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CorsProperties corsProps;

    public SecurityConfig(CorsProperties corsProps) {
        this.corsProps = corsProps;
    }

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
                                                       AuthenticationWebFilter jwtAuthenticationWebFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/destinations/**", "/api/v1/stories/**", "/api/v1/roadmaps/**").permitAll()
                        .pathMatchers("/api/ai/**").permitAll()
                        .pathMatchers("/actuator/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**", "/favicon.ico").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/destinations/**", "/api/v1/stories/**", "/api/v1/recommendation-profiles/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/destinations/**", "/api/v1/stories/**", "/api/v1/recommendation-profiles/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/destinations/**", "/api/v1/stories/**", "/api/v1/recommendation-profiles/**").hasRole("ADMIN")
                        .anyExchange().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::writeUnauthorized)
                        .accessDeniedHandler(this::writeForbidden))
                .build();
    }

    @Bean
    public AuthenticationWebFilter jwtAuthenticationWebFilter(JwtAuthenticationManager jwtAuthenticationManager) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtAuthenticationManager);
        filter.setServerAuthenticationConverter(bearerTokenConverter());
        filter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        filter.setAuthenticationFailureHandler((webFilterExchange, exception) ->
                writeError(webFilterExchange.getExchange(), HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "The access token is invalid or expired"));
        return filter;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    private ServerAuthenticationConverter bearerTokenConverter() {
        return exchange -> Mono.justOrEmpty(resolveBearerToken(exchange))
                .map(token -> UsernamePasswordAuthenticationToken.unauthenticated(token, token))
                .cast(Authentication.class);
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, AuthenticationException ignored) {
        return writeError(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required");
    }

    private Mono<Void> writeForbidden(ServerWebExchange exchange, org.springframework.security.access.AccessDeniedException ignored) {
        return writeError(exchange, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource");
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"success\":false,\"data\":null,\"error\":{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}}";
        var buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String resolveBearerToken(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProps.origins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Correlation-Id", "X-Requested-With"));
        config.setExposedHeaders(List.of("X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
