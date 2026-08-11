import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly accessKey = 'ir_access';
  private readonly refreshKey = 'ir_refresh';

  getAccessToken(): string | null {
    return localStorage.getItem(this.accessKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshKey);
  }

  setTokens(access: string, refresh: string): void {
    // In production, prefer HttpOnly cookies for better protection against XSS.
    localStorage.setItem(this.accessKey, access);
    localStorage.setItem(this.refreshKey, refresh);
  }

  clearTokens(): void {
    localStorage.removeItem(this.accessKey);
    localStorage.removeItem(this.refreshKey);
  }
}
