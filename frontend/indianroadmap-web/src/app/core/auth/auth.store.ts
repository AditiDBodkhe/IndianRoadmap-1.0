import { computed, Injectable, signal } from '@angular/core';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _user = signal<User | null>(null);
  private readonly _loading = signal(false);
  private readonly _initialized = signal(false);

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);
  readonly isLoading = this._loading.asReadonly();
  readonly initialized = this._initialized.asReadonly();

  setUser(user: User | null): void {
    this._user.set(user);
  }

  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  setInitialized(): void {
    this._initialized.set(true);
  }

  clear(): void {
    this._user.set(null);
    this._loading.set(false);
  }
}
