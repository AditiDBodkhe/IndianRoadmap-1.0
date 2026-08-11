package com.indianroadmap.story.dto.response;

import java.util.List;

public record StoryChapterResponse(String chapterId, int sequence, String title, List<StorySectionResponse> sections) {
}
