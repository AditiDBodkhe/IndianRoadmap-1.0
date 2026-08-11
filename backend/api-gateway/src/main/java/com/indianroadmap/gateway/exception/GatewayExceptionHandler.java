package com.indianroadmap.gateway.exception;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        String code;
        String message;

        if (isServiceUnavailable(ex)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            code = "SERVICE_UNAVAILABLE";
            message = "The requested service is temporarily unavailable";
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatus resolved = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            status = resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
            code = status.name();
            message = responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : status.getReasonPhrase();
        } else {
            return Mono.error(ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"success\":false,\"data\":null,\"error\":{\"code\":\"" + code + "\",\"message\":\"" + escape(message) + "\"}}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private boolean isServiceUnavailable(Throwable ex) {
        return ex instanceof ConnectException
                || ex instanceof ConnectTimeoutException
                || ex instanceof ReadTimeoutException
                || ex instanceof TimeoutException
                || (ex.getCause() instanceof ConnectException)
                || (ex.getCause() instanceof ConnectTimeoutException)
                || (ex.getCause() instanceof ReadTimeoutException);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
