import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import {
  AiDestinationRecommendation,
  AiRecommendationsResponse,
  AiRoadmapRecommendation,
  AiSearchResponse,
  DestinationRecommendationRequest,
  MoodRecommendationRequest,
  RoadmapRecommendationRequest,
  SemanticSearchRequest
} from '../models/ai.model';

@Injectable({ providedIn: 'root' })
export class AiApiService {
  private readonly api = inject(ApiClientService);

  recommendByMood(request: MoodRecommendationRequest): Observable<AiRecommendationsResponse<AiDestinationRecommendation>> {
    return this.api.post<AiRecommendationsResponse<AiDestinationRecommendation>>('/api/ai/recommendations/mood', request);
  }

  recommendByDestination(request: DestinationRecommendationRequest): Observable<AiRecommendationsResponse<AiDestinationRecommendation>> {
    return this.api.post<AiRecommendationsResponse<AiDestinationRecommendation>>('/api/ai/recommendations/destination', request);
  }

  recommendRoadmaps(request: RoadmapRecommendationRequest): Observable<AiRecommendationsResponse<AiRoadmapRecommendation>> {
    return this.api.post<AiRecommendationsResponse<AiRoadmapRecommendation>>('/api/ai/recommendations/roadmap', request);
  }

  semanticSearch(request: SemanticSearchRequest): Observable<AiSearchResponse> {
    return this.api.post<AiSearchResponse>('/api/ai/search', request);
  }
}
