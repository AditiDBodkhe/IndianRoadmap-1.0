import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { AppButtonComponent } from '../../../shared/components/app-button/app-button.component';

const passwordMatchValidator: ValidatorFn = (control) => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordMismatch: true } satisfies ValidationErrors;
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppButtonComponent],
  template: `
    <div class="auth-page section">
      <div class="auth-card card">
        <h1>Create your account</h1>
        <p class="subtitle">Begin planning slower, richer journeys across India.</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="name-grid">
            <div class="field">
              <label for="firstName">First name</label>
              <input id="firstName" type="text" formControlName="firstName" />
              @if (form.get('firstName')?.invalid && form.get('firstName')?.touched) {
                <span class="field-error">First name is required</span>
              }
            </div>
            <div class="field">
              <label for="lastName">Last name</label>
              <input id="lastName" type="text" formControlName="lastName" />
              @if (form.get('lastName')?.invalid && form.get('lastName')?.touched) {
                <span class="field-error">Last name is required</span>
              }
            </div>
          </div>
            <div class="field">
              <label for="displayName">Display name (optional)</label>
              <input id="displayName" type="text" formControlName="displayName" />
            </div>
            <div class="field">
              <label for="email">Email</label>
              <input id="email" type="email" formControlName="email" />
              @if (form.get('email')?.invalid && form.get('email')?.touched) {
                <span class="field-error">Enter a valid email</span>
              }
            </div>
            <div class="field">
              <label for="password">Password</label>
              <input id="password" type="password" formControlName="password" />
              @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
                <span class="field-error">Password is required</span>
              }
              @if (form.get('password')?.hasError('minlength') && form.get('password')?.touched) {
                <span class="field-error">Password must be at least 12 characters</span>
              }
            </div>
            <div class="field">
              <label for="confirmPassword">Confirm password</label>
              <input id="confirmPassword" type="password" formControlName="confirmPassword" />
              @if (form.get('confirmPassword')?.hasError('required') && form.get('confirmPassword')?.touched) {
                <span class="field-error">Please confirm your password</span>
              }
              @if (form.hasError('passwordMismatch') && form.get('confirmPassword')?.touched) {
                <span class="field-error">Passwords must match</span>
              }
            </div>
          @if (serverError()) {
            <div class="error-banner">{{ serverError() }}</div>
          }
          <app-button type="submit" [loading]="loading()" [disabled]="form.invalid">Create account</app-button>
        </form>

        <p class="auth-link">Already have an account? <a routerLink="/login">Sign in</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      display: grid;
      place-items: center;
      min-height: 72vh;
    }

    .auth-card,
    form,
    .field {
      display: grid;
      gap: var(--space-4);
    }

    .auth-card {
      width: min(100%, 520px);
    }

    .subtitle,
    .auth-link {
      color: var(--color-text-secondary);
    }

    .name-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
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

    .error-banner {
      padding: var(--space-3);
      border-radius: var(--radius-md);
      border: 1px solid rgba(232, 76, 61, 0.2);
      background: rgba(232, 76, 61, 0.08);
    }

    @media (max-width: 640px) {
      .name-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    lastName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    displayName: new FormControl('', { nonNullable: true }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] }),
    confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  }, { validators: [passwordMatchValidator] });

  readonly loading = signal(false);
  readonly serverError = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.serverError.set(null);
    const { firstName, lastName, displayName, email, password } = this.form.getRawValue();

    this.authService.register({
      firstName,
      lastName,
      displayName: displayName.trim() ? displayName.trim() : undefined,
      email,
      password
    }).subscribe({
      next: () => {
        void this.router.navigate(['/login'], { queryParams: { registered: '1' } });
      },
      error: (err: { error?: { error?: { message?: string; details?: string[] } } }) => {
        const details = err.error?.error?.details;
        this.serverError.set(details?.length ? details.join(', ') : (err.error?.error?.message || 'Unable to create account.'));
        this.loading.set(false);
      }
    });
  }
}
