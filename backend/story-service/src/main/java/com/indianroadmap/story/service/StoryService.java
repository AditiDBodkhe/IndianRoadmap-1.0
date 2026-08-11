package com.indianroadmap.story.service;

import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.document.StoryType;
import com.indianroadmap.story.dto.request.AddChapterRequest;
import com.indianroadmap.story.dto.request.AddSectionRequest;
import com.indianroadmap.story.dto.request.CreateStoryRequest;
import com.indianroadmap.story.dto.request.UpdateChapterRequest;
import com.indianroadmap.story.dto.request.UpdateSectionRequest;
import com.indianroadmap.story.dto.request.UpdateStoryRequest;
import com.indianroadmap.story.dto.response.StoryChapterResponse;
import com.indianroadmap.story.dto.response.StoryResponse;
import com.indianroadmap.story.dto.response.StorySectionResponse;
import com.indianroadmap.story.dto.response.StorySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoryService {

    StoryResponse createStory(CreateStoryRequest request);

    Page<StorySummaryResponse> getStories(StoryStatus status, StoryType storyType, String destinationId, Pageable pageable);

    StoryResponse getStory(String id);

    StoryResponse getStoryBySlug(String slug);

    List<StorySummaryResponse> getStoriesByDestination(String destinationId, StoryStatus status);

    StoryResponse updateStory(String id, UpdateStoryRequest request);

    void deleteStory(String id);

    StoryResponse addChapter(String storyId, AddChapterRequest request);

    List<StoryChapterResponse> getChapters(String storyId);

    StoryChapterResponse getChapter(String storyId, String chapterId);

    StoryResponse updateChapter(String storyId, String chapterId, UpdateChapterRequest request);

    StoryResponse deleteChapter(String storyId, String chapterId);

    StoryResponse addSection(String storyId, String chapterId, AddSectionRequest request);

    List<StorySectionResponse> getSections(String storyId, String chapterId);

    StoryResponse updateSection(String storyId, String chapterId, String sectionId, UpdateSectionRequest request);

    StoryResponse deleteSection(String storyId, String chapterId, String sectionId);

    StoryResponse submitForReview(String id);

    StoryResponse publishStory(String id);

    StoryResponse archiveStory(String id);
}
