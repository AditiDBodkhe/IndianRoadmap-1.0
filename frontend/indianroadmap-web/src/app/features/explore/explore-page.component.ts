import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { DestinationApiService } from '../../core/api/destination-api.service';
import { DestinationMood, DestinationSummary } from '../../core/models/destination.model';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-explore-page',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule, LoadingSpinnerComponent, EmptyStateComponent, ErrorStateComponent],
  template: `
    <section class="section explore-page">
      <div class="container">
        <div class="page-header">
          <div>
            <span class="eyebrow">Explore India</span>
            <h1>Find places that match your curiosity.</h1>
          </div>
          <p>Search, filter by mood, and browse regions with story-rich discovery.</p>
        </div>

        <div class="filters card">
          <div class="filter-grid">
            <label class="field field-wide">
              <span>Search</span>
              <input type="search" [formControl]="searchControl" placeholder="Search by destination, state, or region" />
            </label>
            <label class="field">
              <span>Region</span>
              <input type="text" [formControl]="regionControl" placeholder="Himalayan, Desert, Coastal..." />
            </label>
          </div>
          <div class="chip-row">
            @for (mood of moods; track mood) {
              <button type="button" class="chip selectable" [class.selected]="selectedMood() === mood" (click)="toggleMood(mood)">{{ mood }}</button>
            }
            @if (selectedMood()) {
              <button type="button" class="chip reset-chip" (click)="clearMood()">Clear mood</button>
            }
          </div>
        </div>

        @if (loading()) {
          <div class="center-state"><app-loading-spinner size="lg" /></div>
        } @else if (error()) {
          <app-error-state title="Unable to explore destinations" [message]="error()!" (retry)="loadDestinations(currentPage())" />
        } @else if (visibleDestinations().length === 0) {
          <app-empty-state icon="🧭" title="No destinations match" subtitle="Try a different mood, region, or search term." />
        } @else {
          <div class="results-meta">
            <p>{{ totalElements() }} destinations available</p>
            @if (searchTerm()) {
              <span>Showing {{ visibleDestinations().length }} result(s) on this page for “{{ searchTerm() }}”</span>
            }
          </div>

          <div class="destination-grid">
            @for (destination of visibleDestinations(); track destination.id) {
              <article class="card destination-card">
                <span class="eyebrow">{{ destination.region }}</span>
                <h3>{{ destination.name.defaultName }}</h3>
                <p>{{ destination.state }}</p>
                <div class="chip-row compact">
                  @for (category of destination.categories.slice(0, 2); track category) {
                    <span class="chip">{{ category }}</span>
                  }
                </div>
                <div class="chip-row compact">
                  @for (mood of destination.moods.slice(0, 3); track mood) {
                    <span class="chip mood-chip">{{ mood }}</span>
                  }
                </div>
                <a class="inline-link" [routerLink]="['/destinations', destination.slug]">View destination →</a>
              </article>
            }
          </div>

          <div class="pagination">
            <button type="button" class="btn-secondary" [disabled]="currentPage() === 0" (click)="loadDestinations(currentPage() - 1)">Previous</button>
            <span>Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
            <button type="button" class="btn-secondary" [disabled]="currentPage() + 1 >= totalPages()" (click)="loadDestinations(currentPage() + 1)">Next</button>
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
    .results-meta {
      color: var(--color-text-secondary);
    }

    .filters {
      display: grid;
      gap: var(--space-5);
      margin-bottom: var(--space-8);
    }

    .filter-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: var(--space-4);
    }

    .field {
      display: grid;
      gap: var(--space-2);
    }

    .field span {
      color: var(--color-text-secondary);
      font-size: 0.9rem;
    }

    .chip-row {
      display: flex;
      gap: var(--space-3);
      flex-wrap: wrap;
    }

    .selectable {
      cursor: pointer;
    }

    .selected {
      background: var(--color-accent);
      color: #fff;
      border-color: transparent;
    }

    .reset-chip {
      color: var(--color-accent);
    }

    .results-meta {
      display: flex;
      justify-content: space-between;
      gap: var(--space-3);
      margin-bottom: var(--space-5);
      flex-wrap: wrap;
    }

    .destination-grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: var(--space-5);
    }

    .destination-card {
      display: grid;
      gap: var(--space-3);
    }

    .compact {
      gap: var(--space-2);
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: var(--space-4);
      margin-top: var(--space-8);
    }

    .center-state {
      display: grid;
      place-items: center;
      min-height: 260px;
    }

    @media (max-width: 960px) {
      .filter-grid,
      .destination-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .field-wide {
        grid-column: 1 / -1;
      }
    }

    @media (max-width: 640px) {
      .filter-grid,
      .destination-grid {
        grid-template-columns: 1fr;
      }

      .pagination {
        flex-direction: column;
      }
    }
  `]
})
export class ExplorePageComponent {
  private readonly destinationApi = inject(DestinationApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly regionControl = new FormControl('', { nonNullable: true });
  readonly destinations = signal<DestinationSummary[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly selectedMood = signal<DestinationMood | null>(null);
  readonly totalElements = signal(0);
  readonly totalPages = signal(1);
  readonly currentPage = signal(0);
  readonly searchTerm = signal('');

  readonly moods: DestinationMood[] = ['ZEN', 'ADVENTURE', 'CURIOUS', 'HERITAGE', 'SPIRITUAL', 'SOLITUDE', 'WILD', 'PATRIOTIC'];
  readonly visibleDestinations = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    if (!term) {
      return this.destinations();
    }
    return this.destinations().filter((destination) => {
      const fields = [destination.name.defaultName, destination.state, destination.region, destination.name.localName ?? ''];
      return fields.some((value) => value.toLowerCase().includes(term));
    });
  });

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const mood = params.get('mood') as DestinationMood | null;
      this.selectedMood.set(mood && this.moods.includes(mood) ? mood : null);
      const region = params.get('region') ?? '';
      const page = Number(params.get('page') ?? '0');
      this.regionControl.setValue(region, { emitEvent: false });
      this.loadDestinations(Number.isNaN(page) ? 0 : page);
    });

    this.searchControl.valueChanges.pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed()).subscribe((value) => {
      this.searchTerm.set(value.trim());
    });

    this.regionControl.valueChanges.pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed()).subscribe((value) => {
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { region: value.trim() || null, page: 0 },
        queryParamsHandling: 'merge'
      });
    });
  }

  toggleMood(mood: DestinationMood): void {
    const nextMood = this.selectedMood() === mood ? null : mood;
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { mood: nextMood, page: 0 },
      queryParamsHandling: 'merge'
    });
  }

  clearMood(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { mood: null, page: 0 },
      queryParamsHandling: 'merge'
    });
  }

  loadDestinations(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.currentPage.set(page);
    this.destinationApi.getDestinations({
      page,
      size: 9,
      region: this.regionControl.value.trim() || undefined,
      mood: this.selectedMood() ?? undefined
    }).subscribe({
      next: (response) => {
        this.destinations.set(response.data);
        this.totalElements.set(response.meta.totalElements);
        this.totalPages.set(Math.max(response.meta.totalPages, 1));
        this.currentPage.set(response.meta.page);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load destinations for your selected filters.');
        this.loading.set(false);
      }
    });
  }
}
