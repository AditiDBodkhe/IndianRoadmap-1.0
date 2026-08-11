import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state card">
      <div class="icon">{{ icon() }}</div>
      <h3>{{ title() }}</h3>
      <p>{{ subtitle() }}</p>
    </div>
  `,
  styles: [`
    .empty-state {
      display: grid;
      gap: var(--space-3);
      padding: var(--space-8);
      text-align: center;
      justify-items: center;
    }

    .icon {
      display: grid;
      place-items: center;
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: var(--color-accent-muted);
      font-size: 1.5rem;
    }

    p {
      color: var(--color-text-secondary);
    }
  `]
})
export class EmptyStateComponent {
  readonly icon = input('✦');
  readonly title = input('Nothing here yet');
  readonly subtitle = input('Try adjusting your filters to discover more.');
}
