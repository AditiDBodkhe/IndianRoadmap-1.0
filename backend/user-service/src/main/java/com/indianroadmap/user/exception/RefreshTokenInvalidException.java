package com.indianroadmap.user.exception;

public class RefreshTokenInvalidException extends RuntimeException {

    public RefreshTokenInvalidException() {
        super("Refresh token is invalid");
    }
}
