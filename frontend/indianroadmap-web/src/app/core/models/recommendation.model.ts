export type RecommendationMood = 'ZEN' | 'ADVENTUROUS' | 'SPIRITUAL' | 'CURIOUS' | 'ROMANTIC' | 'CULTURAL' | 'OFFBEAT' | 'SOCIAL' | 'SOLITUDE' | 'FAMILY';
export type Interest = 'NATURE' | 'MOUNTAINS' | 'HISTORY' | 'CULTURE' | 'FOOD' | 'PHOTOGRAPHY' | 'ASTRONOMY' | 'SPIRITUALITY' | 'ADVENTURE' | 'WILDLIFE' | 'ARCHITECTURE' | 'LOCAL_LIFE' | 'ROAD_TRIPS' | 'VILLAGES';
export type TravelStyle = 'BACKPACKER' | 'LUXURY' | 'SLOW_TRAVEL' | 'ROAD_TRIP' | 'SOLO' | 'COUPLE' | 'FAMILY' | 'OFFBEAT' | 'ADVENTURE';
export type Season = 'SPRING' | 'SUMMER' | 'MONSOON' | 'AUTUMN' | 'WINTER';
export type MatchLevel = 'EXCELLENT' | 'GOOD' | 'FAIR';

export interface RecommendationRequest {
  mood: RecommendationMood;
  interests?: Interest[];
  travelStyle?: TravelStyle;
  durationDays?: number;
  maxBudget?: number;
  preferredRegion?: string;
  season?: Season;
  limit?: number;
}

export interface RecommendationResult {
  destination: import('./destination.model').DestinationSummary;
  score: number;
  matchLevel: MatchLevel;
  reasons: string[];
  matchedMoods: string[];
  matchedInterests: string[];
  matchedTravelStyles: string[];
}
