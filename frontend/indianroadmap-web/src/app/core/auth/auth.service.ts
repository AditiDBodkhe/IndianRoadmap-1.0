import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, firstValueFrom, map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { ApiResponse } from '../models/api-response.model';
import { AuthTokens, LoginRequest, RefreshRequest, RegisterRequest, User } from '../models/user.model';
import { TokenStorageService } from '../services/token-storage.service';
import { AuthStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authStore = inject(AuthStore);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly router = inject(Router);
  private readonly baseUrl = APP_CONFIG.apiBaseUrl;

  login(req: LoginRequest): Observable<User> {
    this.authStore.setLoading(true);
    return this.http.post<ApiResponse<AuthTokens>>(`${this.baseUrl}/api/v1/auth/login`, req).pipe(
      tap((response) => this.tokenStorage.setTokens(response.data.accessToken, response.data.refreshToken)),
      switchMap(() => this.loadCurrentUser()),
      finalize(() => this.authStore.setLoading(false))
    );
  }

  register(req: RegisterRequest): Observable<void> {
    return this.http.post<ApiResponse<unknown>>(`${this.baseUrl}/api/v1/auth/register`, req).pipe(map(() => void 0));
  }

  logout(): Observable<void> {
    return this.http.post<ApiResponse<unknown>>(`${this.baseUrl}/api/v1/auth/logout`, {}).pipe(
      map(() => void 0),
      catchError(() => of(void 0)),
      tap(() => {
        this.tokenStorage.clearTokens();
        this.authStore.clear();
        void this.router.navigate(['/login']);
      })
    );
  }

  refreshTokens(): Observable<AuthTokens> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    const request: RefreshRequest = { refreshToken };
    return this.http.post<ApiResponse<AuthTokens>>(`${this.baseUrl}/api/v1/auth/refresh`, request).pipe(
      map((response) => response.data),
      tap((tokens) => this.tokenStorage.setTokens(tokens.accessToken, tokens.refreshToken))
    );
  }

  loadCurrentUser(): Observable<User> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/api/v1/users/me`).pipe(
      map((response) => response.data),
      tap((user) => this.authStore.setUser(user))
    );
  }

  async initializeAuth(): Promise<void> {
    const accessToken = this.tokenStorage.getAccessToken();
    if (!accessToken) {
      this.authStore.setInitialized();
      return;
    }

    this.authStore.setLoading(true);
    await firstValueFrom(
      this.loadCurrentUser().pipe(
        map(() => void 0),
        catchError(() => {
          this.tokenStorage.clearTokens();
          this.authStore.clear();
          return of(void 0);
        }),
        finalize(() => {
          this.authStore.setLoading(false);
          this.authStore.setInitialized();
        })
      )
    );
  }
}
