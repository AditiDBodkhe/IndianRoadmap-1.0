import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse } from '../models/api-response.model';
import { RecommendationMood, RecommendationRequest, RecommendationResult } from '../models/recommendation.model';

@Injectable({ providedIn: 'root' })
export class RecommendationApiService {
  private readonly api = inject(ApiClientService);

  getRecommendations(request: RecommendationRequest): Observable<ApiResponse<RecommendationResult[]>> {
    return this.api.post<ApiResponse<RecommendationResult[]>>('/api/v1/recommendations', request);
  }

  getRecommendationsByMood(mood: RecommendationMood): Observable<ApiResponse<RecommendationResult[]>> {
    return this.api.get<ApiResponse<RecommendationResult[]>>(`/api/v1/recommendations/mood/${mood}`);
  }

  getSimilarDestinations(destinationId: string): Observable<ApiResponse<RecommendationResult[]>> {
    return this.api.get<ApiResponse<RecommendationResult[]>>(`/api/v1/recommendations/destination/${destinationId}/similar`);
  }
}
