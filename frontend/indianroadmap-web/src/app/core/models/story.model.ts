export type StoryType = 'CULTURAL' | 'HISTORICAL' | 'ADVENTURE' | 'SPIRITUAL' | 'FOOD' | 'NATURE' | 'LOCAL_LEGEND';
export type StoryStatus = 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'ARCHIVED';
export type StoryDifficulty = 'EASY' | 'MODERATE' | 'CHALLENGING';
export type StoryLanguage = 'ENGLISH' | 'HINDI' | 'PUNJABI' | 'TAMIL' | 'TELUGU' | 'KANNADA' | 'MALAYALAM' | 'BENGALI';

export interface StorySection {
  id: string;
  title?: string;
  content: string;
  order: number;
}

export interface StoryChapter {
  id: string;
  title: string;
  sections: StorySection[];
  order: number;
}

export interface StorySummary {
  id: string;
  slug: string;
  destinationId: string;
  title: string;
  shortDescription?: string;
  storyType: StoryType;
  status: StoryStatus;
  difficulty: StoryDifficulty;
  availableLanguages: StoryLanguage[];
  estimatedReadingTimeMinutes: number;
  createdAt: string;
  updatedAt: string;
}

export interface Story extends StorySummary {
  chapters: StoryChapter[];
  publishedAt?: string;
}
