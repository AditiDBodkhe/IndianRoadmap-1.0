import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiClientService } from './api-client.service';
import { ApiResponse } from '../models/api-response.model';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly api = inject(ApiClientService);

  getCurrentUser(): Observable<ApiResponse<User>> {
    return this.api.get<ApiResponse<User>>('/api/v1/users/me');
  }

  updateProfile(updates: Partial<Pick<User, 'firstName' | 'lastName' | 'displayName' | 'bio'>>): Observable<ApiResponse<User>> {
    return this.api.patch<ApiResponse<User>>('/api/v1/users/me', updates);
  }
}
