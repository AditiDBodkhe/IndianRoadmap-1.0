import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse } from '../models/api-response.model';
import { Audio, GenerateAudioRequest } from '../models/audio.model';

@Injectable({ providedIn: 'root' })
export class AudioApiService {
  private readonly api = inject(ApiClientService);

  generateAudio(request: GenerateAudioRequest): Observable<ApiResponse<Audio>> {
    return this.api.post<ApiResponse<Audio>>('/api/v1/audio', request);
  }

  getAudio(id: string): Observable<ApiResponse<Audio>> {
    return this.api.get<ApiResponse<Audio>>(`/api/v1/audio/${id}`);
  }
}
