import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse, PagedApiResponse } from '../models/api-response.model';
import { Story, StorySummary } from '../models/story.model';

@Injectable({ providedIn: 'root' })
export class StoryApiService {
  private readonly api = inject(ApiClientService);

  getStories(params?: { page?: number; size?: number; destinationId?: string }): Observable<PagedApiResponse<StorySummary>> {
    const queryParams: Record<string, string | number> = {};
    if (params?.page !== undefined) {
      queryParams['page'] = params.page;
    }
    if (params?.size !== undefined) {
      queryParams['size'] = params.size;
    }
    if (params?.destinationId) {
      queryParams['destinationId'] = params.destinationId;
    }
    return this.api.get<PagedApiResponse<StorySummary>>('/api/v1/stories', queryParams);
  }

  getStoryBySlug(slug: string): Observable<ApiResponse<Story>> {
    return this.api.get<ApiResponse<Story>>(`/api/v1/stories/slug/${slug}`);
  }

  getStoryById(id: string): Observable<ApiResponse<Story>> {
    return this.api.get<ApiResponse<Story>>(`/api/v1/stories/${id}`);
  }

  getStoriesByDestination(destinationId: string): Observable<ApiResponse<StorySummary[]>> {
    return this.api.get<ApiResponse<StorySummary[]>>(`/api/v1/stories/destination/${destinationId}`);
  }
}
