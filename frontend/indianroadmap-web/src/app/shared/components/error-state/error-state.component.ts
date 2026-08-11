import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  template: `
    <div class="error-state card">
      <h3>{{ title() }}</h3>
      <p>{{ message() }}</p>
      <button type="button" class="btn-secondary" (click)="retry.emit()">Try again</button>
    </div>
  `,
  styles: [`
    .error-state {
      display: grid;
      gap: var(--space-3);
      padding: var(--space-8);
      text-align: center;
    }

    p {
      color: var(--color-text-secondary);
    }
  `]
})
export class ErrorStateComponent {
  readonly title = input('Something went wrong');
  readonly message = input('Please try again in a moment.');
  readonly retry = output<void>();
}
