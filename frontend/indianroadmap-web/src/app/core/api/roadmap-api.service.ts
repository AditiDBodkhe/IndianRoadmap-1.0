import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse, PagedApiResponse } from '../models/api-response.model';
import { Roadmap, RoadmapSummary } from '../models/roadmap.model';

@Injectable({ providedIn: 'root' })
export class RoadmapApiService {
  private readonly api = inject(ApiClientService);

  getRoadmaps(params?: { page?: number; size?: number }): Observable<PagedApiResponse<RoadmapSummary>> {
    const queryParams: Record<string, string | number> = {};
    if (params?.page !== undefined) queryParams['page'] = params.page;
    if (params?.size !== undefined) queryParams['size'] = params.size;
    return this.api.get<PagedApiResponse<RoadmapSummary>>('/api/v1/roadmaps', queryParams);
  }

  getRoadmap(id: string): Observable<ApiResponse<Roadmap>> {
    return this.api.get<ApiResponse<Roadmap>>(`/api/v1/roadmaps/${id}`);
  }

  getRoadmapBySlug(slug: string): Observable<ApiResponse<Roadmap>> {
    return this.api.get<ApiResponse<Roadmap>>(`/api/v1/roadmaps/slug/${slug}`);
  }
}
