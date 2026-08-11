import { CommonModule } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { DestinationApiService } from '../../core/api/destination-api.service';
import { StoryApiService } from '../../core/api/story-api.service';
import { Destination } from '../../core/models/destination.model';
import { StorySummary } from '../../core/models/story.model';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-destination-page',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent, ErrorStateComponent],
  template: `
    <section class="section destination-page">
      <div class="container">
        @if (loading()) {
          <div class="center-state"><app-loading-spinner size="lg" /></div>
        } @else if (error()) {
          <app-error-state title="Destination unavailable" [message]="error()!" (retry)="reload()" />
        } @else if (destination()) {
          <div class="hero card">
            <div>
              <span class="eyebrow">{{ destination()!.region }} · {{ destination()!.state }}</span>
              <h1>{{ destination()!.name.defaultName }}</h1>
              @if (destination()!.name.localName) {
                <p class="local-name">{{ destination()!.name.localName }}</p>
              }
              <p class="description">{{ destination()!.shortDescription || destination()!.description || 'A destination full of untold cultural richness.' }}</p>
              <div class="chip-row">
                @for (mood of destination()!.moods; track mood) {
                  <span class="chip">{{ mood }}</span>
                }
              </div>
            </div>
            <div class="metrics">
              <div class="metric"><span>Elevation</span><strong>{{ destination()!.elevation.meters }} m</strong></div>
              <div class="metric"><span>District</span><strong>{{ destination()!.district || '—' }}</strong></div>
              <div class="metric"><span>Verified</span><strong>{{ destination()!.verified ? 'Yes' : 'No' }}</strong></div>
            </div>
          </div>

          <div class="content-grid">
            <article class="card prose-card">
              <h2>About this destination</h2>
              <p>{{ destination()!.description || destination()!.shortDescription || 'Detailed destination writing will appear here as content is published.' }}</p>
              <dl class="detail-list">
                <div>
                  <dt>Coordinates</dt>
                  <dd>{{ destination()!.coordinates.latitude }}, {{ destination()!.coordinates.longitude }}</dd>
                </div>
                <div>
                  <dt>Categories</dt>
                  <dd>{{ destination()!.categories.join(', ') }}</dd>
                </div>
              </dl>
            </article>

            <aside class="card stories-card">
              <h2>Stories from here</h2>
              @if (storiesLoading()) {
                <app-loading-spinner />
              } @else if (stories().length === 0) {
                <p class="muted">No stories are published for this destination yet.</p>
              } @else {
                <div class="story-list">
                  @for (story of stories(); track story.id) {
                    <article class="story-item">
                      <p class="story-type">{{ story.storyType }}</p>
                      <h3>{{ story.title }}</h3>
                      <p>{{ story.shortDescription || 'A narrative crafted for immersive discovery.' }}</p>
                      <span>{{ story.estimatedReadingTimeMinutes }} min read</span>
                    </article>
                  }
                </div>
              }
            </aside>
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    .hero,
    .content-grid {
      display: grid;
      gap: var(--space-6);
    }

    .hero {
      grid-template-columns: 1.3fr 0.7fr;
      margin-bottom: var(--space-8);
      background: linear-gradient(180deg, rgba(232, 96, 44, 0.08), transparent 65%), var(--color-surface);
    }

    .description,
    .muted,
    .story-item p,
    .story-item span,
    .detail-list dt {
      color: var(--color-text-secondary);
    }

    .local-name {
      margin-top: var(--space-2);
      font-family: var(--font-display);
      font-size: 1.15rem;
      color: var(--color-accent);
    }

    .metrics {
      display: grid;
      gap: var(--space-4);
      align-content: start;
    }

    .metric {
      padding: var(--space-4);
      border: 1px solid var(--color-border);
      border-radius: var(--radius-lg);
      background: rgba(255, 255, 255, 0.02);
    }

    .metric span,
    .detail-list dt {
      display: block;
      margin-bottom: var(--space-2);
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }

    .metric strong {
      font-size: 1.1rem;
    }

    .content-grid {
      grid-template-columns: 1.1fr 0.9fr;
    }

    .prose-card,
    .stories-card {
      display: grid;
      gap: var(--space-4);
    }

    .detail-list {
      display: grid;
      gap: var(--space-4);
    }

    .story-list {
      display: grid;
      gap: var(--space-4);
    }

    .story-item {
      padding: var(--space-4);
      border-radius: var(--radius-lg);
      border: 1px solid var(--color-border);
      background: rgba(255, 255, 255, 0.02);
    }

    .story-type {
      margin-bottom: var(--space-2);
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--color-accent) !important;
      font-size: 0.8rem;
    }

    .chip-row {
      display: flex;
      gap: var(--space-3);
      flex-wrap: wrap;
      margin-top: var(--space-4);
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 260px;
    }

    @media (max-width: 960px) {
      .hero,
      .content-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DestinationPageComponent {
  private readonly destinationApi = inject(DestinationApiService);
  private readonly storyApi = inject(StoryApiService);

  readonly slug = input.required<string>();
  readonly destination = signal<Destination | null>(null);
  readonly stories = signal<StorySummary[]>([]);
  readonly loading = signal(true);
  readonly storiesLoading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const currentSlug = this.slug();
      if (currentSlug) {
        this.fetchDestination(currentSlug);
      }
    });
  }

  reload(): void {
    this.fetchDestination(this.slug());
  }

  private fetchDestination(slug: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.destinationApi.getDestinationBySlug(slug).subscribe({
      next: (response) => {
        this.destination.set(response.data);
        this.loading.set(false);
        this.loadStories(response.data.id);
      },
      error: () => {
        this.error.set('We could not load this destination right now.');
        this.loading.set(false);
      }
    });
  }

  private loadStories(destinationId: string): void {
    this.storiesLoading.set(true);
    this.storyApi.getStoriesByDestination(destinationId).subscribe({
      next: (response) => {
        this.stories.set(response.data);
        this.storiesLoading.set(false);
      },
      error: () => {
        this.stories.set([]);
        this.storiesLoading.set(false);
      }
    });
  }
}
