export type AiBudget = 'BUDGET' | 'MID_RANGE' | 'PREMIUM';

export interface MoodRecommendationRequest {
  mood: string;
  durationDays?: number;
  budget?: AiBudget;
}

export interface DestinationRecommendationRequest {
  destinationId: string;
  preferences?: string[];
}

export interface RoadmapRecommendationRequest {
  mood?: string;
  region?: string;
  durationDays?: number;
}

export interface SemanticSearchRequest {
  query: string;
  limit?: number;
}

export interface AiDestinationRecommendation {
  destinationId: string;
  destinationSlug: string;
  destinationName: string;
  score: number;
  matchLevel?: string;
  reason: string;
}

export interface AiRoadmapRecommendation {
  roadmapId: string;
  roadmapSlug: string;
  title: string;
  score: number;
  estimatedDistanceKm?: number;
  nodeCount?: number;
  reason: string;
}

export interface AiSearchResult {
  type: 'destination' | 'roadmap' | 'story';
  id: string;
  slug: string;
  title: string;
  subtitle: string;
  score: number;
  path: string;
}

export interface AiRecommendationsResponse<T> {
  success: boolean;
  recommendations: T[];
}

export interface AiSearchResponse {
  success: boolean;
  query: string;
  results: AiSearchResult[];
}
