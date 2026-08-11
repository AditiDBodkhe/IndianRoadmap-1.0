package com.indianroadmap.audio.service;

import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.AudioStatus;
import com.indianroadmap.audio.dto.request.GenerateAudioRequest;
import com.indianroadmap.audio.dto.request.RegenerateAudioRequest;
import com.indianroadmap.audio.dto.response.AudioResponse;
import com.indianroadmap.audio.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AudioService {

    AudioResponse generateAudio(GenerateAudioRequest request);

    AudioResponse getAudio(String audioId);

    PageResponse<AudioResponse> listByStory(String storyId, AudioLanguage language, AudioStatus status,
                                             Pageable pageable);

    PageResponse<AudioResponse> listBySection(String sectionId, AudioLanguage language, AudioStatus status,
                                               Pageable pageable);

    AudioResponse regenerateAudio(String audioId, RegenerateAudioRequest request);

    void deleteAudio(String audioId);

    byte[] getAudioContent(String audioId);
}
