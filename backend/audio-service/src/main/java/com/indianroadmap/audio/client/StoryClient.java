package com.indianroadmap.audio.client;

import com.indianroadmap.audio.exception.StoryNotFoundException;
import com.indianroadmap.audio.exception.StoryServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class StoryClient {

    private final RestClient restClient;

    public StoryClient(@Qualifier("audioStoryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public StorySectionSummary getSection(String storyId, String chapterId, String sectionId) {
        try {
            SectionListResponse response = restClient.get()
                    .uri("/api/v1/stories/{storyId}/chapters/{chapterId}/sections",
                            storyId, chapterId)
                    .retrieve()
                    .body(SectionListResponse.class);

            if (response == null || !response.success() || response.data() == null) {
                throw new StoryServiceUnavailableException("Story service returned empty response");
            }

            return response.data().stream()
                    .filter(s -> sectionId.equals(s.sectionId()))
                    .map(s -> new StorySectionSummary(s.sectionId(), s.sequence(), s.heading(),
                            s.content(), s.language()))
                    .findFirst()
                    .orElseThrow(() -> new StoryNotFoundException(
                            "Section not found: storyId=%s chapterId=%s sectionId=%s"
                                    .formatted(storyId, chapterId, sectionId)));
        } catch (StoryNotFoundException | StoryServiceUnavailableException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new StoryNotFoundException("Chapter not found: storyId=%s chapterId=%s"
                    .formatted(storyId, chapterId));
        } catch (HttpClientErrorException ex) {
            throw new StoryServiceUnavailableException("Story service returned client error: " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            throw new StoryServiceUnavailableException("Story service unavailable", ex);
        } catch (RestClientResponseException ex) {
            throw new StoryServiceUnavailableException("Story service returned server error", ex);
        } catch (RestClientException ex) {
            throw new StoryServiceUnavailableException("Story service unavailable", ex);
        }
    }

    private record SectionListResponse(boolean success, java.util.List<SectionData> data) {}

    private record SectionData(
            String sectionId,
            int sequence,
            String heading,
            String content,
            com.indianroadmap.audio.document.AudioLanguage language
    ) {}
}
