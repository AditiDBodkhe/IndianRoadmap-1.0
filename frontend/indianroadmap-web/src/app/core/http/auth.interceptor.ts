import { HttpContextToken, HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';
import { AuthStore } from '../auth/auth.store';
import { TokenStorageService } from '../services/token-storage.service';

const RETRY_ONCE = new HttpContextToken<boolean>(() => false);
let isRefreshing = false;

function cleanupAndRedirect(tokenStorage: TokenStorageService, authStore: AuthStore, router: Router): void {
  tokenStorage.clearTokens();
  authStore.clear();
  void router.navigate(['/login']);
}

function stripUnsafeHeaders(request: HttpRequest<unknown>): HttpRequest<unknown> {
  const headers = request.headers.delete('X-User-Id').delete('X-User-Role');
  return request.clone({ headers });
}

function isPublicAuthRequest(url: string): boolean {
  return url.includes('/api/v1/auth/login')
    || url.includes('/api/v1/auth/register')
    || url.includes('/api/v1/auth/refresh');
}

export const authInterceptor: HttpInterceptorFn = (req, next: HttpHandlerFn) => {
  const tokenStorage = inject(TokenStorageService);
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  const gatewayRequest = req.url.startsWith(environment.apiBaseUrl);
  const refreshRequest = req.url.includes('/api/v1/auth/refresh');
  const publicAuthRequest = isPublicAuthRequest(req.url);
  const alreadyRetried = req.context.get(RETRY_ONCE);

  let sanitizedRequest = stripUnsafeHeaders(req);
  const accessToken = tokenStorage.getAccessToken();

  if (gatewayRequest && accessToken && !publicAuthRequest) {
    sanitizedRequest = sanitizedRequest.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });
  }

  return next(sanitizedRequest).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || !gatewayRequest || refreshRequest || alreadyRetried) {
        return throwError(() => error);
      }

      const refreshToken = tokenStorage.getRefreshToken();
      if (!refreshToken || isRefreshing) {
        cleanupAndRedirect(tokenStorage, authStore, router);
        return throwError(() => error);
      }

      isRefreshing = true;
      return authService.refreshTokens().pipe(
        switchMap((tokens) => {
          const retryRequest = sanitizedRequest.clone({
            context: sanitizedRequest.context.set(RETRY_ONCE, true),
            setHeaders: {
              Authorization: `Bearer ${tokens.accessToken}`
            }
          });
          return next(retryRequest);
        }),
        catchError((refreshError: unknown) => {
          cleanupAndRedirect(tokenStorage, authStore, router);
          return throwError(() => refreshError);
        }),
        finalize(() => {
          isRefreshing = false;
        })
      );
    })
  );
};
