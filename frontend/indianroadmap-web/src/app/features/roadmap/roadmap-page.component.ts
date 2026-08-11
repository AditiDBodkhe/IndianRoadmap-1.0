import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RoadmapApiService } from '../../core/api/roadmap-api.service';
import { RoadmapSummary } from '../../core/models/roadmap.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-roadmap-page',
  standalone: true,
  imports: [CommonModule, RouterLink, EmptyStateComponent, ErrorStateComponent, LoadingSpinnerComponent],
  template: `
    <section class="section">
      <div class="container">
        <div class="page-header">
          <span class="eyebrow">Protected experience</span>
          <h1>Your cultural roadmaps</h1>
          <p>Browse curated routes and prepare for a richer interactive planning canvas.</p>
        </div>

        @if (loading()) {
          <div class="center-state"><app-loading-spinner size="lg" /></div>
        } @else if (error()) {
          <app-error-state title="Unable to load roadmaps" [message]="error()!" (retry)="loadRoadmaps()" />
        } @else if (roadmaps().length === 0) {
          <app-empty-state icon="🗺️" title="No roadmaps yet" subtitle="Create or publish roadmaps once roadmap data is available from the gateway." />
        } @else {
          <div class="roadmap-grid">
            @for (roadmap of roadmaps(); track roadmap.id) {
              <article class="card roadmap-card">
                <span class="eyebrow">{{ roadmap.status }}</span>
                <h3>{{ roadmap.name }}</h3>
                <p>{{ roadmap.description || 'A multi-stop travel storyline across India.' }}</p>
                <dl>
                  <div><dt>Nodes</dt><dd>{{ roadmap.nodeCount }}</dd></div>
                  <div><dt>Edges</dt><dd>{{ roadmap.edgeCount }}</dd></div>
                  <div><dt>Distance</dt><dd>{{ roadmap.totalDistanceKm }} km</dd></div>
                </dl>
                <a class="btn-primary" [routerLink]="['/roadmaps', roadmap.slug]">Explore roadmap</a>
              </article>
            }
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    .page-header {
      display: grid;
      gap: var(--space-3);
      margin-bottom: var(--space-8);
    }

    .page-header p,
    .roadmap-card p,
    dt,
    .coming-soon span {
      color: var(--color-text-secondary);
    }

    .roadmap-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: var(--space-5);
    }

    .roadmap-card {
      display: grid;
      gap: var(--space-4);
    }

    dl {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: var(--space-4);
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 260px;
    }

    @media (max-width: 768px) {
      .roadmap-grid,
      dl {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class RoadmapPageComponent {
  private readonly roadmapApi = inject(RoadmapApiService);

  readonly roadmaps = signal<RoadmapSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.loadRoadmaps();
  }

  loadRoadmaps(): void {
    this.loading.set(true);
    this.error.set(null);
    this.roadmapApi.getRoadmaps({ page: 0, size: 50 }).subscribe({
      next: (response) => {
        this.roadmaps.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Roadmaps are unavailable right now.');
        this.loading.set(false);
      }
    });
  }
}
