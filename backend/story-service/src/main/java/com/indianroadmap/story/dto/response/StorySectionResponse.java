package com.indianroadmap.story.dto.response;

import com.indianroadmap.story.document.StoryLanguage;

public record StorySectionResponse(String sectionId, int sequence, String heading, String content, StoryLanguage language) {
}
