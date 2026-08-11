package com.indianroadmap.story.service;

import com.indianroadmap.story.calculator.ReadingTimeCalculator;
import com.indianroadmap.story.client.DestinationClient;
import com.indianroadmap.story.document.StoryChapterDocument;
import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StoryLanguage;
import com.indianroadmap.story.document.StorySectionDocument;
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
import com.indianroadmap.story.exception.ChapterNotFoundException;
import com.indianroadmap.story.exception.DuplicateStoryException;
import com.indianroadmap.story.exception.InvalidStoryStructureException;
import com.indianroadmap.story.exception.SectionNotFoundException;
import com.indianroadmap.story.exception.StoryNotFoundException;
import com.indianroadmap.story.mapper.StoryMapper;
import com.indianroadmap.story.repository.StoryRepository;
import com.indianroadmap.story.validation.StoryStructureValidator;
import com.indianroadmap.story.validation.StoryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final StoryMapper storyMapper;
    private final DestinationClient destinationClient;
    private final StoryValidator storyValidator;
    private final StoryStructureValidator structureValidator;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final Clock clock;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public StoryServiceImpl(StoryRepository storyRepository, StoryMapper storyMapper,
                            DestinationClient destinationClient, StoryValidator storyValidator,
                            StoryStructureValidator structureValidator,
                            ReadingTimeCalculator readingTimeCalculator, Clock clock,
                            MongoTemplate mongoTemplate) {
        this.storyRepository = storyRepository;
        this.storyMapper = storyMapper;
        this.destinationClient = destinationClient;
        this.storyValidator = storyValidator;
        this.structureValidator = structureValidator;
        this.readingTimeCalculator = readingTimeCalculator;
        this.clock = clock;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public StoryResponse createStory(CreateStoryRequest request) {
        storyValidator.validateCreateRequest(request);
        String normalizedSlug = storyValidator.normalizeSlug(request.slug());
        if (storyRepository.existsBySlug(normalizedSlug)) {
            throw new DuplicateStoryException(normalizedSlug);
        }

        destinationClient.getDestination(request.destinationId().trim());
        CreateStoryRequest normalizedRequest = new CreateStoryRequest(
            normalizedSlug,
            request.destinationId().trim(),
            request.title().trim(),
            request.shortDescription(),
            request.storyType(),
            request.difficulty(),
            request.availableLanguages() == null ? List.of() : List.copyOf(request.availableLanguages())
        );

        StoryDocument document = storyMapper.mapToDocument(normalizedRequest, clock);
        document.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(document));
        StoryDocument saved = storyRepository.save(document);
        return storyMapper.mapToResponse(saved);
    }

    @Override
    public Page<StorySummaryResponse> getStories(StoryStatus status, StoryType storyType, String destinationId, Pageable pageable) {
        Query query = new Query().with(pageable);
        Query countQuery = new Query();
        List<Criteria> conditions = new ArrayList<>();
        if (status != null) {
            conditions.add(Criteria.where("status").is(status));
        }
        if (storyType != null) {
            conditions.add(Criteria.where("storyType").is(storyType));
        }
        if (destinationId != null && !destinationId.isBlank()) {
            conditions.add(Criteria.where("destinationId").is(destinationId));
        }
        if (!conditions.isEmpty()) {
            Criteria criteria = new Criteria().andOperator(conditions.toArray(new Criteria[0]));
            query.addCriteria(criteria);
            countQuery.addCriteria(criteria);
        }

        long total = mongoTemplate.count(countQuery, StoryDocument.class);
        List<StoryDocument> docs = mongoTemplate.find(query, StoryDocument.class);
        return new PageImpl<>(docs.stream().map(storyMapper::mapToSummaryResponse).toList(), pageable, total);
    }

    @Override
    public StoryResponse getStory(String id) {
        return storyMapper.mapToResponse(getStoryDocument(id));
    }

    @Override
    public StoryResponse getStoryBySlug(String slug) {
        String normalizedSlug = storyValidator.normalizeSlug(slug);
        StoryDocument story = storyRepository.findBySlug(normalizedSlug)
            .orElseThrow(() -> new StoryNotFoundException(normalizedSlug));
        return storyMapper.mapToResponse(story);
    }

    @Override
    public List<StorySummaryResponse> getStoriesByDestination(String destinationId, StoryStatus status) {
        List<StoryDocument> stories = status == null
            ? storyRepository.findByDestinationId(destinationId)
            : storyRepository.findByDestinationIdAndStatus(destinationId, status);
        return List.copyOf(stories.stream()
            .map(storyMapper::mapToSummaryResponse)
            .toList());
    }

    @Override
    public StoryResponse updateStory(String id, UpdateStoryRequest request) {
        StoryDocument story = getStoryDocument(id);
        story.setTitle(request.title().trim());
        story.setShortDescription(request.shortDescription());
        if (request.difficulty() != null) {
            story.setDifficulty(request.difficulty());
        }
        if (request.availableLanguages() != null) {
            validateExistingSectionLanguages(story, request.availableLanguages());
            story.setAvailableLanguages(request.availableLanguages());
        }
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public void deleteStory(String id) {
        StoryDocument story = getStoryDocument(id);
        storyRepository.delete(story);
    }

    @Override
    public StoryResponse addChapter(String storyId, AddChapterRequest request) {
        StoryDocument story = getStoryDocument(storyId);
        List<StoryChapterDocument> chapters = sortChapters(story.getChapters());

        StoryChapterDocument chapter = new StoryChapterDocument();
        chapter.setChapterId(UUID.randomUUID().toString());
        chapter.setTitle(request.title().trim());
        chapter.setSequence(request.sequence());

        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, chapters.size()));
        chapters.add(insertIndex, chapter);
        resequenceChapters(chapters);
        structureValidator.validateChapterSequences(chapters);
        story.setChapters(chapters);
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public List<StoryChapterResponse> getChapters(String storyId) {
        StoryDocument story = getStoryDocument(storyId);
        return List.copyOf(sortChapters(story.getChapters()).stream()
            .map(storyMapper::mapChapterToResponse)
            .toList());
    }

    @Override
    public StoryChapterResponse getChapter(String storyId, String chapterId) {
        StoryDocument story = getStoryDocument(storyId);
        return storyMapper.mapChapterToResponse(findChapter(story, chapterId));
    }

    @Override
    public StoryResponse updateChapter(String storyId, String chapterId, UpdateChapterRequest request) {
        StoryDocument story = getStoryDocument(storyId);
        List<StoryChapterDocument> chapters = sortChapters(story.getChapters());
        StoryChapterDocument chapter = chapters.stream()
            .filter(item -> item.getChapterId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new ChapterNotFoundException(chapterId));

        chapters.remove(chapter);
        chapter.setTitle(request.title().trim());
        chapter.setSequence(request.sequence());
        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, chapters.size()));
        chapters.add(insertIndex, chapter);
        resequenceChapters(chapters);
        structureValidator.validateChapterSequences(chapters);
        story.setChapters(chapters);
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse deleteChapter(String storyId, String chapterId) {
        StoryDocument story = getStoryDocument(storyId);
        StoryChapterDocument chapter = findChapter(story, chapterId);
        story.getChapters().remove(chapter);
        resequenceChapters(story.getChapters());
        structureValidator.validateChapterSequences(story.getChapters());
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse addSection(String storyId, String chapterId, AddSectionRequest request) {
        StoryDocument story = getStoryDocument(storyId);
        StoryChapterDocument chapter = findChapter(story, chapterId);
        structureValidator.validateSection(story, chapter, request);

        List<StorySectionDocument> sections = sortSections(chapter.getSections());
        StorySectionDocument section = new StorySectionDocument();
        section.setSectionId(UUID.randomUUID().toString());
        section.setHeading(request.heading());
        section.setContent(request.content());
        section.setSequence(request.sequence());
        section.setLanguage(request.language());

        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, sections.size()));
        sections.add(insertIndex, section);
        resequenceSections(sections);
        structureValidator.validateSectionSequences(sections);
        chapter.setSections(sections);
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public List<StorySectionResponse> getSections(String storyId, String chapterId) {
        StoryDocument story = getStoryDocument(storyId);
        StoryChapterDocument chapter = findChapter(story, chapterId);
        return List.copyOf(sortSections(chapter.getSections()).stream()
            .map(storyMapper::mapSectionToResponse)
            .toList());
    }

    @Override
    public StoryResponse updateSection(String storyId, String chapterId, String sectionId, UpdateSectionRequest request) {
        StoryDocument story = getStoryDocument(storyId);
        StoryChapterDocument chapter = findChapter(story, chapterId);
        List<StorySectionDocument> sections = sortSections(chapter.getSections());
        StorySectionDocument section = sections.stream()
            .filter(item -> item.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new SectionNotFoundException(sectionId));

        sections.remove(section);
        StoryChapterDocument validationChapter = new StoryChapterDocument(chapter.getChapterId(), chapter.getSequence(), chapter.getTitle(), sections);
        structureValidator.validateSection(story, validationChapter,
            new AddSectionRequest(request.heading(), request.content(), request.sequence(), request.language()));

        section.setHeading(request.heading());
        section.setContent(request.content());
        section.setSequence(request.sequence());
        section.setLanguage(request.language());
        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, sections.size()));
        sections.add(insertIndex, section);
        resequenceSections(sections);
        structureValidator.validateSectionSequences(sections);
        chapter.setSections(sections);
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse deleteSection(String storyId, String chapterId, String sectionId) {
        StoryDocument story = getStoryDocument(storyId);
        StoryChapterDocument chapter = findChapter(story, chapterId);
        StorySectionDocument section = findSection(chapter, sectionId);
        chapter.getSections().remove(section);
        resequenceSections(chapter.getSections());
        structureValidator.validateSectionSequences(chapter.getSections());
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse submitForReview(String id) {
        StoryDocument story = getStoryDocument(id);
        storyValidator.validateStatusTransition(story.getStatus(), StoryStatus.REVIEW);
        structureValidator.validateForReview(story);
        story.setStatus(StoryStatus.REVIEW);
        touch(story);
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse publishStory(String id) {
        StoryDocument story = getStoryDocument(id);
        storyValidator.validateStatusTransition(story.getStatus(), StoryStatus.PUBLISHED);
        structureValidator.validateForPublishing(story);
        story.setStatus(StoryStatus.PUBLISHED);
        story.setPublishedAt(Instant.now(clock));
        touch(story);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    @Override
    public StoryResponse archiveStory(String id) {
        StoryDocument story = getStoryDocument(id);
        storyValidator.validateStatusTransition(story.getStatus(), StoryStatus.ARCHIVED);
        story.setStatus(StoryStatus.ARCHIVED);
        touch(story);
        return storyMapper.mapToResponse(storyRepository.save(story));
    }

    private StoryDocument getStoryDocument(String id) {
        return storyRepository.findById(id).orElseThrow(() -> new StoryNotFoundException(id));
    }

    private StoryChapterDocument findChapter(StoryDocument story, String chapterId) {
        return story.getChapters().stream()
            .filter(chapter -> chapter.getChapterId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new ChapterNotFoundException(chapterId));
    }

    private StorySectionDocument findSection(StoryChapterDocument chapter, String sectionId) {
        return chapter.getSections().stream()
            .filter(section -> section.getSectionId().equals(sectionId))
            .findFirst()
            .orElseThrow(() -> new SectionNotFoundException(sectionId));
    }

    private List<StoryChapterDocument> sortChapters(List<StoryChapterDocument> chapters) {
        return new ArrayList<>(chapters.stream()
            .sorted(Comparator.comparingInt(StoryChapterDocument::getSequence))
            .toList());
    }

    private List<StorySectionDocument> sortSections(List<StorySectionDocument> sections) {
        return new ArrayList<>(sections.stream()
            .sorted(Comparator.comparingInt(StorySectionDocument::getSequence))
            .toList());
    }

    private void resequenceChapters(List<StoryChapterDocument> chapters) {
        chapters.sort(Comparator.comparingInt(StoryChapterDocument::getSequence));
        for (int index = 0; index < chapters.size(); index++) {
            chapters.get(index).setSequence(index + 1);
        }
    }

    private void resequenceSections(List<StorySectionDocument> sections) {
        sections.sort(Comparator.comparingInt(StorySectionDocument::getSequence));
        for (int index = 0; index < sections.size(); index++) {
            sections.get(index).setSequence(index + 1);
        }
    }

    private void touch(StoryDocument story) {
        story.setUpdatedAt(Instant.now(clock));
    }

    private void validateExistingSectionLanguages(StoryDocument story, List<StoryLanguage> availableLanguages) {
        for (StoryChapterDocument chapter : story.getChapters()) {
            for (StorySectionDocument section : chapter.getSections()) {
                if (!availableLanguages.contains(section.getLanguage())) {
                    throw new InvalidStoryStructureException("All existing section languages must remain available on the story");
                }
            }
        }
    }
}
