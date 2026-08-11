import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, filter, map, of, switchMap, tap } from 'rxjs';
import { AiApiService } from '../../core/api/ai-api.service';
import { DestinationApiService } from '../../core/api/destination-api.service';
import { AiDestinationRecommendation, AiSearchResult } from '../../core/models/ai.model';
import { DestinationSummary } from '../../core/models/destination.model';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoadingSpinnerComponent, ErrorStateComponent],
  template: `
    <section class="hero section">
      <div class="container hero-grid">
        <div class="hero-copy">
          <span class="eyebrow">Immersive travel discovery</span>
          <h1>Discover India. Beyond the obvious.</h1>
          <p>
            Wander through soulful villages, mountain monasteries, living heritage, and stories that turn every route into a cultural experience.
          </p>
          <div class="hero-actions">
            <a class="btn-primary" routerLink="/explore">Explore destinations</a>
            <a class="btn-secondary" routerLink="/recommendations">Get recommendations</a>
          </div>
        </div>
        <div class="hero-panel card">
          <p class="hero-stat-label">AI semantic search</p>
          <input
            class="search-input"
            type="search"
            placeholder="Try: heritage road trip in Rajasthan"
            [formControl]="searchControl"
          />
          @if (searchLoading()) {
            <div class="search-state">Searching across destinations, roadmaps, and stories...</div>
          } @else if (searchError()) {
            <div class="search-state">{{ searchError() }}</div>
          } @else if (searchControl.value.trim().length >= 3) {
            <div class="search-results">
              @for (result of searchResults(); track result.type + '-' + result.id) {
                <a class="search-result" [routerLink]="result.path">
                  <span class="chip">{{ result.type }}</span>
                  <strong>{{ result.title }}</strong>
                  <span>{{ result.subtitle }}</span>
                </a>
              } @empty {
                <div class="search-state">No close matches yet. Try a place, mood, or travel style.</div>
              }
            </div>
          } @else {
            <div class="search-state">Search works with natural language and uses live service data.</div>
          }
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container section-heading">
        <div>
          <span class="eyebrow">Featured destinations</span>
          <h2>Places worth slowing down for</h2>
        </div>
        <a routerLink="/explore">See all</a>
      </div>

      <div class="container">
        @if (loading()) {
          <div class="center-state"><app-loading-spinner size="lg" /></div>
        } @else if (error()) {
          <app-error-state title="Unable to load destinations" [message]="error()!" (retry)="loadFeatured()" />
        } @else {
          <div class="card-grid">
            @for (destination of destinations(); track destination.id) {
              <a class="destination-card card" [routerLink]="['/destinations', destination.slug]">
                <div class="destination-card__content">
                  <span class="eyebrow">{{ destination.region }}</span>
                  <h3>{{ destination.name.defaultName }}</h3>
                  <p>{{ destination.state }}</p>
                  <div class="chip-row">
                    @for (mood of destination.moods.slice(0, 3); track mood) {
                      <span class="chip">{{ mood }}</span>
                    }
                  </div>
                </div>
              </a>
            }
          </div>
        }
      </div>
    </section>

    <section class="section muted-section">
      <div class="container section-heading">
        <div>
          <span class="eyebrow">Explore by mood</span>
          <h2>Choose the energy of your next journey</h2>
        </div>
      </div>
      <div class="container mood-grid">
        @for (mood of moods; track mood) {
          <button
            type="button"
            class="mood-chip"
            [class.active]="selectedMood() === mood"
            (click)="loadMoodRecommendations(mood)"
          >
            {{ mood }}
          </button>
        }
      </div>
      <div class="container mood-results">
        @if (moodLoading()) {
          <div class="center-state"><app-loading-spinner /></div>
        } @else if (moodError()) {
          <app-error-state title="Could not load mood recommendations" [message]="moodError()!" (retry)="loadMoodRecommendations(selectedMood())" />
        } @else {
          <div class="card-grid">
            @for (item of moodRecommendations(); track item.destinationId) {
              <a class="destination-card card" [routerLink]="['/destinations', item.destinationSlug]">
                <div class="destination-card__content">
                  <span class="eyebrow">{{ selectedMood() }}</span>
                  <h3>{{ item.destinationName }}</h3>
                  <p>{{ item.reason }}</p>
                  <span class="chip">{{ (item.score * 100).toFixed(0) }}% match</span>
                </div>
              </a>
            } @empty {
              <div class="search-state">No recommendations available for this mood right now.</div>
            }
          </div>
        }
      </div>
    </section>

    <section class="section">
      <div class="container cta card">
        <div>
          <span class="eyebrow">Start your journey</span>
          <h2>Build a roadmap that feels deeply yours.</h2>
          <p>Unlock personalised suggestions, save story-rich destinations, and plan meaningful routes across India.</p>
        </div>
        <a class="btn-primary" routerLink="/register">Create your account</a>
      </div>
    </section>
  `,
  styles: [`
    .hero {
      padding-top: var(--space-16);
    }

    .hero-grid,
    .cta {
      display: grid;
      grid-template-columns: 1.3fr 0.7fr;
      gap: var(--space-8);
      align-items: center;
    }

    .hero-copy h1 {
      max-width: 11ch;
      margin: var(--space-4) 0;
      font-size: clamp(3rem, 5vw, 5.4rem);
    }

    .hero-copy p,
    .cta p {
      max-width: 620px;
      color: var(--color-text-secondary);
      font-size: 1.05rem;
    }

    .hero-actions {
      display: flex;
      gap: var(--space-4);
      flex-wrap: wrap;
      margin-top: var(--space-6);
    }

    .hero-panel {
      padding: var(--space-8);
      min-height: 280px;
      display: grid;
      align-content: start;
      gap: var(--space-3);
      background: radial-gradient(circle at top, rgba(232, 96, 44, 0.18), transparent 50%), var(--color-surface);
    }

    .search-input {
      width: 100%;
      border-radius: var(--radius-md);
      border: 1px solid var(--color-border);
      padding: 0.75rem 0.9rem;
      background: rgba(255, 255, 255, 0.02);
      color: var(--color-text-primary);
    }

    .search-results {
      display: grid;
      gap: 0.7rem;
    }

    .search-result {
      display: grid;
      gap: 0.3rem;
      padding: 0.75rem;
      border-radius: var(--radius-md);
      border: 1px solid var(--color-border);
      background: rgba(255, 255, 255, 0.02);
    }

    .search-state {
      color: var(--color-text-secondary);
      font-size: 0.92rem;
    }

    .hero-stat-label,
    .destination-card p,
    .cta p {
      color: var(--color-text-secondary);
    }

    .section-heading {
      display: flex;
      justify-content: space-between;
      align-items: end;
      gap: var(--space-4);
      margin-bottom: var(--space-8);
    }

    .card-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: var(--space-5);
    }

    .destination-card {
      min-height: 220px;
      background: linear-gradient(180deg, rgba(232, 96, 44, 0.08), transparent 60%), var(--color-surface);
      transition: transform var(--transition-fast), border-color var(--transition-fast);
    }

    .destination-card:hover {
      transform: translateY(-4px);
      border-color: var(--color-border-strong);
    }

    .destination-card__content {
      display: grid;
      gap: var(--space-3);
      height: 100%;
      padding: var(--space-6);
    }

    .chip-row,
    .mood-grid {
      display: flex;
      gap: var(--space-3);
      flex-wrap: wrap;
    }

    .mood-chip {
      padding: 0.9rem 1.1rem;
      border-radius: var(--radius-full);
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      cursor: pointer;
    }

    .mood-chip.active {
      border-color: var(--color-border-strong);
      box-shadow: 0 0 0 1px rgba(232, 96, 44, 0.55) inset;
    }

    .muted-section {
      background: linear-gradient(180deg, rgba(19, 19, 26, 0.35), rgba(19, 19, 26, 0));
    }

    .mood-results {
      margin-top: var(--space-6);
    }

    .cta {
      padding: var(--space-8);
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 240px;
    }

    @media (max-width: 960px) {
      .card-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .hero-grid,
      .cta {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 640px) {
      .card-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class HomePageComponent {
  private readonly destinationApi = inject(DestinationApiService);
  private readonly aiApi = inject(AiApiService);

  readonly destinations = signal<DestinationSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly moods = ['ZEN', 'ADVENTURE', 'SPIRITUAL', 'CURIOUS', 'HERITAGE', 'SOLITUDE', 'WILD', 'PATRIOTIC'] as const;
  readonly selectedMood = signal<string>('ZEN');
  readonly moodRecommendations = signal<AiDestinationRecommendation[]>([]);
  readonly moodLoading = signal(false);
  readonly moodError = signal<string | null>(null);
  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly searchResults = signal<AiSearchResult[]>([]);
  readonly searchLoading = signal(false);
  readonly searchError = signal<string | null>(null);

  constructor() {
    this.loadFeatured();
    this.loadMoodRecommendations(this.selectedMood());
    this.setupSemanticSearch();
  }

  loadFeatured(): void {
    this.loading.set(true);
    this.error.set(null);
    this.destinationApi.getDestinations({ size: 6 }).subscribe({
      next: (response) => {
        this.destinations.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Featured destinations could not be loaded.');
        this.loading.set(false);
      }
    });
  }

  loadMoodRecommendations(mood: string): void {
    this.selectedMood.set(mood);
    this.moodLoading.set(true);
    this.moodError.set(null);
    this.aiApi.recommendByMood({ mood }).subscribe({
      next: (response) => {
        this.moodRecommendations.set(response.recommendations);
        this.moodLoading.set(false);
      },
      error: () => {
        this.moodRecommendations.set([]);
        this.moodError.set('Mood recommendations are unavailable at the moment.');
        this.moodLoading.set(false);
      }
    });
  }

  private setupSemanticSearch(): void {
    this.searchControl.valueChanges.pipe(
      map((value) => value.trim()),
      debounceTime(300),
      distinctUntilChanged(),
      tap((query) => {
        if (query.length < 3) {
          this.searchResults.set([]);
          this.searchError.set(null);
          this.searchLoading.set(false);
        }
      }),
      filter((query) => query.length >= 3),
      tap(() => {
        this.searchLoading.set(true);
        this.searchError.set(null);
      }),
      switchMap((query) =>
        this.aiApi.semanticSearch({ query, limit: 8 }).pipe(
          map((response) => response.results),
          catchError(() => {
            this.searchError.set('Search is temporarily unavailable.');
            return of([]);
          })
        )
      ),
      takeUntilDestroyed()
    ).subscribe((results) => {
      this.searchResults.set(results);
      this.searchLoading.set(false);
    });
  }
}
