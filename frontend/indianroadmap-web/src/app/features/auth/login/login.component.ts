import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page section">
      <div class="auth-card card">
        <h1>Welcome back</h1>
        <p class="subtitle">Sign in to IndianRoadmap</p>

        @if (route.snapshot.queryParamMap.get('registered')) {
          <div class="info-banner">Your account is ready. Sign in to continue.</div>
        }

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="field">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" placeholder="you@example.com" />
            @if (form.get('email')?.invalid && form.get('email')?.touched) {
              <span class="field-error">Valid email is required</span>
            }
          </div>
          <div class="field">
            <label for="password">Password</label>
            <input id="password" type="password" formControlName="password" placeholder="••••••••••••" />
            @if (form.get('password')?.invalid && form.get('password')?.touched) {
              <span class="field-error">Password is required</span>
            }
          </div>
          @if (errorMessage()) {
            <div class="error-banner">{{ errorMessage() }}</div>
          }
          <button type="submit" [disabled]="loading() || form.invalid" class="btn-primary btn-full">
            @if (loading()) { <span class="spinner-sm"></span> }
            Sign in
          </button>
        </form>
        <p class="auth-link">Don't have an account? <a routerLink="/register">Create one</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      display: grid;
      place-items: center;
      min-height: 72vh;
    }

    .auth-card {
      width: min(100%, 460px);
      display: grid;
      gap: var(--space-5);
    }

    .subtitle,
    .auth-link {
      color: var(--color-text-secondary);
    }

    form,
    .field {
      display: grid;
      gap: var(--space-4);
    }

    .field {
      gap: var(--space-2);
    }

    .field-error,
    .error-banner {
      color: var(--color-error);
      font-size: 0.9rem;
    }

    .info-banner,
    .error-banner {
      padding: var(--space-3);
      border-radius: var(--radius-md);
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid var(--color-border);
    }

    .info-banner {
      color: var(--color-info);
    }

    .btn-full {
      width: 100%;
    }

    .spinner-sm {
      display: inline-block;
      width: 14px;
      height: 14px;
      margin-right: var(--space-2);
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.35);
      border-top-color: #fff;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoginComponent {
  protected readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    const { email, password } = this.form.getRawValue();

    this.authService.login({ email, password }).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/profile';
        void this.router.navigateByUrl(returnUrl);
      },
      error: (err: { error?: { error?: { message?: string } } }) => {
        this.errorMessage.set(err.error?.error?.message || 'Invalid credentials');
        this.loading.set(false);
      }
    });
  }
}
