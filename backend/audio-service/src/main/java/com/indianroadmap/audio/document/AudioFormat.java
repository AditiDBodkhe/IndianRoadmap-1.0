package com.indianroadmap.audio.document;

public enum AudioFormat {
    MP3,
    WAV,
    OGG;

    public String contentType() {
        return switch (this) {
            case MP3 -> "audio/mpeg";
            case WAV -> "audio/wav";
            case OGG -> "audio/ogg";
        };
    }

    public String extension() {
        return switch (this) {
            case MP3 -> "mp3";
            case WAV -> "wav";
            case OGG -> "ogg";
        };
    }
}
