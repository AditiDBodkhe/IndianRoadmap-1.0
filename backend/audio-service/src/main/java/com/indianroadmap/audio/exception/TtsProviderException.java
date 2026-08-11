package com.indianroadmap.audio.exception;

public class TtsProviderException extends RuntimeException {
    public TtsProviderException(String message) {
        super(message);
    }
    public TtsProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
