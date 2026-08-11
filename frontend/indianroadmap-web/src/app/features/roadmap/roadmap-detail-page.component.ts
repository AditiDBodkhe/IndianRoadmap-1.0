import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DestinationApiService } from '../../core/api/destination-api.service';
import { RoadmapApiService } from '../../core/api/roadmap-api.service';
import { Destination } from '../../core/models/destination.model';
import { Roadmap } from '../../core/models/roadmap.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-roadmap-detail-page',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent, ErrorStateComponent, EmptyStateComponent],
  template: `
    <section class="section">
      <div class="container">
        @if (loading()) {
          <div class="center-state"><app-loading-spinner size="lg" /></div>
        } @else if (error()) {
          <app-error-state title="Unable to load roadmap" [message]="error()!" (retry)="reload()" />
        } @else if (!roadmap()) {
          <app-empty-state icon="🗺️" title="Roadmap not found" subtitle="The requested roadmap does not exist." />
        } @else {
          <header class="card header">
            <div>
              <span class="eyebrow">{{ roadmap()!.status }}</span>
              <h1>{{ roadmap()!.name }}</h1>
              <p>{{ roadmap()!.description }}</p>
            </div>
            <div class="stats">
              <div><span>Distance</span><strong>{{ roadmap()!.routeSummary.totalDistanceKm.toFixed(0) }} km</strong></div>
              <div><span>Travel time</span><strong>{{ (roadmap()!.routeSummary.totalTravelTimeMinutes / 60).toFixed(1) }} hrs</strong></div>
              <div><span>Elevation gain</span><strong>{{ roadmap()!.routeSummary.elevationGainMeters }} m</strong></div>
            </div>
          </header>

          <section class="grid">
            <article class="card">
              <h2>Route path</h2>
              <svg class="route-svg" viewBox="0 0 900 280" preserveAspectRatio="xMidYMid meet">
                @for (edge of roadmap()!.edges; track edge.edgeId) {
                  <line
                    [attr.x1]="nodePoint(edge.fromNodeId).x"
                    [attr.y1]="nodePoint(edge.fromNodeId).y"
                    [attr.x2]="nodePoint(edge.toNodeId).x"
                    [attr.y2]="nodePoint(edge.toNodeId).y"
                    stroke="var(--color-accent)"
                    stroke-width="3"
                    stroke-linecap="round"
                  />
                }
                @for (node of sortedNodes(); track node.nodeId) {
                  <circle
                    [attr.cx]="nodePoint(node.nodeId).x"
                    [attr.cy]="nodePoint(node.nodeId).y"
                    r="10"
                    fill="var(--color-surface-elevated)"
                    stroke="var(--color-accent)"
                    stroke-width="3"
                  />
                  <text
                    [attr.x]="nodePoint(node.nodeId).x"
                    [attr.y]="nodePoint(node.nodeId).y - 16"
                    text-anchor="middle"
                    fill="var(--color-text-secondary)"
                    font-size="12"
                  >{{ node.label }}</text>
                }
              </svg>
              <div class="node-list">
                @for (node of sortedNodes(); track node.nodeId) {
                  <button type="button" class="node-item" (click)="selectNodeDestination(node.destinationId)">
                    <strong>{{ node.sequence }}. {{ node.label }}</strong>
                    <span>{{ node.role }} · {{ node.elevationMeters }} m</span>
                  </button>
                }
              </div>
            </article>

            <aside class="card">
              <h2>Selected destination</h2>
              @if (destinationLoading()) {
                <app-loading-spinner />
              } @else if (selectedDestination()) {
                <h3>{{ selectedDestination()!.name.defaultName }}</h3>
                <p>{{ selectedDestination()!.shortDescription || selectedDestination()!.description }}</p>
                <p class="meta">{{ selectedDestination()!.state }} · {{ selectedDestination()!.region }}</p>
                <a class="btn-secondary" [routerLink]="['/destinations', selectedDestination()!.slug]">Explore destination</a>
              } @else {
                <p class="meta">Select a node to load destination details.</p>
              }
            </aside>
          </section>
        }
      </div>
    </section>
  `,
  styles: [`
    .header {
      display: grid;
      grid-template-columns: 1.3fr 0.7fr;
      gap: var(--space-6);
      margin-bottom: var(--space-6);
    }
    .header p, .meta, .stats span { color: var(--color-text-secondary); }
    .stats { display: grid; gap: var(--space-4); align-content: start; }
    .stats > div { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-4); }
    .stats span { display: block; font-size: 0.85rem; margin-bottom: var(--space-2); }
    .grid { display: grid; grid-template-columns: 1.2fr 0.8fr; gap: var(--space-5); }
    .route-svg { width: 100%; border: 1px solid var(--color-border); border-radius: var(--radius-md); background: rgba(255,255,255,0.02); margin-bottom: var(--space-4); }
    .node-list { display: grid; gap: var(--space-3); }
    .node-item { text-align: left; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-text-primary); border-radius: var(--radius-md); padding: var(--space-3); }
    .node-item span { display: block; color: var(--color-text-secondary); font-size: 0.85rem; margin-top: var(--space-1); }
    .center-state { display: grid; place-items: center; min-height: 260px; }
    @media (max-width: 960px) { .header, .grid { grid-template-columns: 1fr; } }
  `]
})
export class RoadmapDetailPageComponent {
  private readonly roadmapApi = inject(RoadmapApiService);
  private readonly destinationApi = inject(DestinationApiService);

  readonly slug = input.required<string>();
  readonly roadmap = signal<Roadmap | null>(null);
  readonly selectedDestination = signal<Destination | null>(null);
  readonly loading = signal(true);
  readonly destinationLoading = signal(false);
  readonly error = signal<string | null>(null);

  readonly sortedNodes = computed(() =>
    [...(this.roadmap()?.nodes ?? [])].sort((a, b) => a.sequence - b.sequence)
  );

  constructor() {
    effect(() => {
      const currentSlug = this.slug();
      if (currentSlug) this.fetchRoadmap(currentSlug);
    });
  }

  reload(): void {
    this.fetchRoadmap(this.slug());
  }

  nodePoint(nodeId: string): { x: number; y: number } {
    const nodes = this.sortedNodes();
    const index = nodes.findIndex((n) => n.nodeId === nodeId);
    if (index < 0 || nodes.length === 0) return { x: 40, y: 140 };
    const x = 60 + index * (780 / Math.max(nodes.length - 1, 1));
    const y = 140 + (index % 2 === 0 ? -35 : 35);
    return { x, y };
  }

  selectNodeDestination(destinationId: string): void {
    this.destinationLoading.set(true);
    this.destinationApi.getDestinationById(destinationId).subscribe({
      next: (response) => {
        this.selectedDestination.set(response.data);
        this.destinationLoading.set(false);
      },
      error: () => {
        this.selectedDestination.set(null);
        this.destinationLoading.set(false);
      }
    });
  }

  private fetchRoadmap(slug: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.selectedDestination.set(null);
    this.roadmapApi.getRoadmapBySlug(slug).subscribe({
      next: (response) => {
        this.roadmap.set(response.data);
        this.loading.set(false);
        const firstNode = response.data.nodes.slice().sort((a, b) => a.sequence - b.sequence)[0];
        if (firstNode) this.selectNodeDestination(firstNode.destinationId);
      },
      error: () => {
        this.error.set('Roadmap details are unavailable right now.');
        this.loading.set(false);
      }
    });
  }
}
