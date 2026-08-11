import { CommonModule } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserApiService } from '../../core/api/user-api.service';
import { AuthStore } from '../../core/auth/auth.store';
import { User } from '../../core/models/user.model';
import { AppButtonComponent } from '../../shared/components/app-button/app-button.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AppButtonComponent, LoadingSpinnerComponent],
  template: `
    <section class="section">
      <div class="container profile-layout">
        <div class="card profile-summary">
          @if (loading()) {
            <div class="center-state"><app-loading-spinner /></div>
          } @else if (user()) {
            <div class="summary-content">
              <span class="eyebrow">{{ user()!.role }}</span>
              <h1>{{ displayName() }}</h1>
              <p>{{ user()!.email }}</p>
              <div class="meta-grid">
                <div><span>Status</span><strong>{{ user()!.status }}</strong></div>
                <div><span>Joined</span><strong>{{ user()!.createdAt | date:'mediumDate' }}</strong></div>
                <div><span>Last login</span><strong>{{ user()!.lastLoginAt ? (user()!.lastLoginAt | date:'medium') : '—' }}</strong></div>
              </div>
            </div>
          }
        </div>

        <form class="card profile-form" [formGroup]="form" (ngSubmit)="save()">
          <div>
            <span class="eyebrow">Profile</span>
            <h2>Edit your details</h2>
          </div>

          <div class="grid-two">
            <label class="field">
              <span>First name</span>
              <input type="text" formControlName="firstName" />
            </label>
            <label class="field">
              <span>Last name</span>
              <input type="text" formControlName="lastName" />
            </label>
          </div>

          <label class="field">
            <span>Display name</span>
            <input type="text" formControlName="displayName" />
          </label>

          <label class="field">
            <span>Bio</span>
            <textarea rows="5" formControlName="bio"></textarea>
          </label>

          @if (message()) {
            <p class="message">{{ message() }}</p>
          }

          <app-button type="submit" [loading]="saving()" [disabled]="form.invalid">Save profile</app-button>
        </form>
      </div>
    </section>
  `,
  styles: [`
    .profile-layout {
      display: grid;
      grid-template-columns: 0.85fr 1.15fr;
      gap: var(--space-6);
      align-items: start;
    }

    .profile-summary,
    .profile-form,
    .field {
      display: grid;
      gap: var(--space-4);
    }

    .profile-summary p,
    .message,
    .field span,
    .meta-grid span {
      color: var(--color-text-secondary);
    }

    .meta-grid,
    .grid-two {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: var(--space-4);
    }

    .meta-grid div {
      padding: var(--space-4);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-lg);
      background: rgba(255, 255, 255, 0.02);
    }

    .field {
      gap: var(--space-2);
    }

    .message {
      min-height: 1.4rem;
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 180px;
    }

    @media (max-width: 960px) {
      .profile-layout,
      .meta-grid,
      .grid-two {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProfilePageComponent {
  private readonly authStore = inject(AuthStore);
  private readonly userApi = inject(UserApiService);

  readonly form = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    lastName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    displayName: new FormControl('', { nonNullable: true }),
    bio: new FormControl('', { nonNullable: true })
  });
  readonly user = signal<User | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly message = signal('');

  constructor() {
    effect(() => {
      const currentUser = this.authStore.user();
      if (currentUser) {
        this.syncForm(currentUser);
        this.loading.set(false);
      }
    });

    this.userApi.getCurrentUser().subscribe({
      next: (response) => {
        this.user.set(response.data);
        this.authStore.setUser(response.data);
        this.syncForm(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  displayName(): string {
    const currentUser = this.user() ?? this.authStore.user();
    if (!currentUser) {
      return 'Traveller';
    }
    return currentUser.displayName || `${currentUser.firstName} ${currentUser.lastName}`;
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.message.set('');
    const updates = this.form.getRawValue();
    this.userApi.updateProfile(updates).subscribe({
      next: (response) => {
        this.user.set(response.data);
        this.authStore.setUser(response.data);
        this.syncForm(response.data);
        this.message.set('Profile updated successfully.');
        this.saving.set(false);
      },
      error: () => {
        this.message.set('Unable to update profile right now.');
        this.saving.set(false);
      }
    });
  }

  private syncForm(user: User): void {
    this.user.set(user);
    this.form.patchValue({
      firstName: user.firstName,
      lastName: user.lastName,
      displayName: user.displayName ?? '',
      bio: user.bio ?? ''
    }, { emitEvent: false });
  }
}
