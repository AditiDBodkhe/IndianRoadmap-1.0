import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RecommendationApiService } from '../../core/api/recommendation-api.service';
import { Interest, RecommendationMood, RecommendationRequest, RecommendationResult, TravelStyle } from '../../core/models/recommendation.model';
import { AppButtonComponent } from '../../shared/components/app-button/app-button.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-recommendation-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AppButtonComponent, EmptyStateComponent, LoadingSpinnerComponent],
  template: `
    <section class="section recommendation-page">
      <div class="container recommendation-grid">
        <form class="card form-card" (ngSubmit)="submit()">
          <div>
            <span class="eyebrow">Protected recommendations</span>
            <h1>Tell us how you want to travel.</h1>
            <p>Blend mood, interests, and travel style to get personalised destination matches.</p>
          </div>

          <label class="field">
            <span>Mood</span>
            <select [formControl]="moodControl">
              <option value="">Select a mood</option>
              @for (mood of moods; track mood) {
                <option [value]="mood">{{ mood }}</option>
              }
            </select>
          </label>

          <label class="field">
            <span>Travel style</span>
            <select [formControl]="travelStyleControl">
              <option value="">Any style</option>
              @for (style of travelStyles; track style) {
                <option [value]="style">{{ style }}</option>
              }
            </select>
          </label>

          <div class="field">
            <span>Interests</span>
            <div class="chip-row">
              @for (interest of interests; track interest) {
                <button type="button" class="chip selectable" [class.selected]="selectedInterests().includes(interest)" (click)="toggleInterest(interest)">{{ interest }}</button>
              }
            </div>
          </div>

          <app-button type="submit" [loading]="loading()" [disabled]="!moodControl.value">Get recommendations</app-button>
        </form>

        <div class="results-column">
          @if (loading()) {
            <div class="center-state card"><app-loading-spinner size="lg" /></div>
          } @else if (results().length === 0) {
            <app-empty-state icon="✨" title="No recommendations yet" subtitle="Choose a mood and let IndianRoadmap suggest destinations." />
          } @else {
            <div class="results-list">
              @for (result of results(); track result.destination.id) {
                <article class="card result-card">
                  <div class="result-header">
                    <div>
                      <span class="eyebrow">{{ result.matchLevel }} match</span>
                      <h3>{{ result.destination.name.defaultName }}</h3>
                      <p>{{ result.destination.state }} · {{ result.destination.region }}</p>
                    </div>
                    <strong>{{ result.score | number:'1.0-0' }}%</strong>
                  </div>
                  <div class="chip-row compact">
                    @for (reason of result.reasons; track reason) {
                      <span class="chip">{{ reason }}</span>
                    }
                  </div>
                  <a class="inline-link" [routerLink]="['/destinations', result.destination.slug]">Open destination →</a>
                </article>
              }
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [`
    .recommendation-grid {
      display: grid;
      grid-template-columns: 0.9fr 1.1fr;
      gap: var(--space-6);
      align-items: start;
    }

    .form-card,
    .results-list,
    .result-card {
      display: grid;
      gap: var(--space-5);
    }

    p,
    .result-card p {
      color: var(--color-text-secondary);
    }

    .field {
      display: grid;
      gap: var(--space-2);
    }

    .chip-row {
      display: flex;
      flex-wrap: wrap;
      gap: var(--space-3);
    }

    .selectable {
      cursor: pointer;
    }

    .selected {
      background: var(--color-accent);
      color: #fff;
      border-color: transparent;
    }

    .result-header {
      display: flex;
      justify-content: space-between;
      gap: var(--space-4);
      align-items: start;
    }

    .compact {
      gap: var(--space-2);
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 260px;
    }

    @media (max-width: 960px) {
      .recommendation-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class RecommendationPageComponent {
  private readonly recommendationApi = inject(RecommendationApiService);

  readonly moodControl = new FormControl<RecommendationMood | ''>('', { validators: [Validators.required], nonNullable: true });
  readonly travelStyleControl = new FormControl<TravelStyle | ''>('', { nonNullable: true });
  readonly loading = signal(false);
  readonly results = signal<RecommendationResult[]>([]);
  readonly selectedInterests = signal<Interest[]>([]);

  readonly moods: RecommendationMood[] = ['ZEN', 'ADVENTUROUS', 'SPIRITUAL', 'CURIOUS', 'ROMANTIC', 'CULTURAL', 'OFFBEAT', 'SOCIAL', 'SOLITUDE', 'FAMILY'];
  readonly interests: Interest[] = ['NATURE', 'MOUNTAINS', 'HISTORY', 'CULTURE', 'FOOD', 'PHOTOGRAPHY', 'ASTRONOMY', 'SPIRITUALITY', 'ADVENTURE', 'WILDLIFE', 'ARCHITECTURE', 'LOCAL_LIFE', 'ROAD_TRIPS', 'VILLAGES'];
  readonly travelStyles: TravelStyle[] = ['BACKPACKER', 'LUXURY', 'SLOW_TRAVEL', 'ROAD_TRIP', 'SOLO', 'COUPLE', 'FAMILY', 'OFFBEAT', 'ADVENTURE'];

  toggleInterest(interest: Interest): void {
    this.selectedInterests.update((items) => items.includes(interest) ? items.filter((item) => item !== interest) : [...items, interest]);
  }

  submit(): void {
    if (!this.moodControl.value) {
      this.moodControl.markAsTouched();
      return;
    }

    const payload: RecommendationRequest = {
      mood: this.moodControl.value,
      interests: this.selectedInterests().length > 0 ? this.selectedInterests() : undefined,
      travelStyle: this.travelStyleControl.value || undefined,
      limit: 6
    };

    this.loading.set(true);
    this.recommendationApi.getRecommendations(payload).subscribe({
      next: (response) => {
        this.results.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.results.set([]);
        this.loading.set(false);
      }
    });
  }
}
