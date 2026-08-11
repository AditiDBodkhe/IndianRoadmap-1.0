import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `<span class="spinner" [style.width]="dimension()" [style.height]="dimension()"></span>`,
  styles: [`
    .spinner {
      display: inline-block;
      border-radius: 50%;
      border: 3px solid rgba(255, 255, 255, 0.15);
      border-top-color: var(--color-accent);
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingSpinnerComponent {
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly dimension = computed(() => {
    switch (this.size()) {
      case 'sm':
        return '18px';
      case 'lg':
        return '40px';
      default:
        return '28px';
    }
  });
}
