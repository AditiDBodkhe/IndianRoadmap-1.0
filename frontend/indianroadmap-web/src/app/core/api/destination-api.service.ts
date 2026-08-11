import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse, PagedApiResponse } from '../models/api-response.model';
import { Destination, DestinationMood, DestinationSummary } from '../models/destination.model';

@Injectable({ providedIn: 'root' })
export class DestinationApiService {
  private readonly api = inject(ApiClientService);

  getDestinations(params?: { page?: number; size?: number; region?: string; mood?: DestinationMood }): Observable<PagedApiResponse<DestinationSummary>> {
    const queryParams: Record<string, string | number> = {};
    if (params?.page !== undefined) queryParams['page'] = params.page;
    if (params?.size !== undefined) queryParams['size'] = params.size;
    if (params?.region) queryParams['region'] = params.region;
    if (params?.mood) queryParams['mood'] = params.mood;
    return this.api.get<PagedApiResponse<DestinationSummary>>('/api/v1/destinations', queryParams);
  }

  getDestinationBySlug(slug: string): Observable<ApiResponse<Destination>> {
    return this.api.get<ApiResponse<Destination>>(`/api/v1/destinations/slug/${slug}`);
  }

  getDestinationById(id: string): Observable<ApiResponse<Destination>> {
    return this.api.get<ApiResponse<Destination>>(`/api/v1/destinations/${id}`);
  }

  getNearbyDestinations(lat: number, lng: number, radius = 50000): Observable<ApiResponse<DestinationSummary[]>> {
    return this.api.get<ApiResponse<DestinationSummary[]>>('/api/v1/destinations/nearby', { latitude: lat, longitude: lng, radius });
  }
}
