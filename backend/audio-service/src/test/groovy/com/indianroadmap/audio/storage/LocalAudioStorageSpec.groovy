package com.indianroadmap.audio.storage

import com.indianroadmap.audio.config.AudioStorageProperties
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class LocalAudioStorageSpec extends Specification {

    @TempDir
    Path tempDir

    @Subject
    LocalAudioStorage storage

    def setup() {
        storage = new LocalAudioStorage(new AudioStorageProperties(tempDir.toString()))
    }

    def "store creates file and returns relative path"() {
        given:
        byte[] bytes = "mock audio data".bytes

        when:
        String path = storage.store("story-1", "section-1", "english", "default", 1, "mp3", bytes)

        then:
        path != null
        !path.isBlank()
        Files.exists(tempDir.resolve(path))
    }

    def "read returns stored bytes"() {
        given:
        byte[] original = "test audio content".bytes
        String path = storage.store("story-2", "section-2", "hindi", "default", 1, "mp3", original)

        when:
        byte[] read = storage.read(path)

        then:
        read == original
    }

    def "delete removes file"() {
        given:
        byte[] bytes = "delete test".bytes
        String path = storage.store("story-3", "section-3", "english", "default", 1, "mp3", bytes)

        when:
        storage.delete(path)

        then:
        !Files.exists(tempDir.resolve(path))
    }

    def "delete handles missing file gracefully"() {
        when:
        storage.delete("nonexistent/path/audio.mp3")

        then:
        noExceptionThrown()
    }

    def "delete with null path is a no-op"() {
        when:
        storage.delete(null)

        then:
        noExceptionThrown()
    }

    def "directories are created automatically"() {
        given:
        byte[] bytes = "bytes".bytes

        when:
        String path = storage.store("new-story", "new-section", "english", "voice", 1, "wav", bytes)

        then:
        Files.exists(tempDir.resolve(path))
    }

    def "same content stored at different versions gives different paths"() {
        given:
        byte[] bytes = "content".bytes

        when:
        String path1 = storage.store("story-x", "section-x", "english", "default", 1, "mp3", bytes)
        String path2 = storage.store("story-x", "section-x", "english", "default", 2, "mp3", bytes)

        then:
        path1 != path2
    }
}
