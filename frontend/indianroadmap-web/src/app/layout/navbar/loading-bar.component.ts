import { Component, inject } from '@angular/core';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-loading-bar',
  standalone: true,
  template: `
    @if (loadingService.isLoading()) {
      <div class="loading-bar"></div>
    }
  `,
  styles: [`
    .loading-bar {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 3px;
      z-index: 999;
      background: linear-gradient(90deg, var(--color-accent), #ffb35c, var(--color-accent));
      background-size: 200% 100%;
      animation: pulse-bar 1.2s linear infinite;
    }

    @keyframes pulse-bar {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class LoadingBarComponent {
  protected readonly loadingService = inject(LoadingService);
}
