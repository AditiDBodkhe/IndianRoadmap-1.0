import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notification-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-stack">
      @for (notification of notificationService.notifications(); track notification.id) {
        <article class="toast" [class]="'toast toast-' + notification.type">
          <p>{{ notification.message }}</p>
          <button type="button" (click)="notificationService.dismiss(notification.id)">×</button>
        </article>
      }
    </div>
  `,
  styles: [`
    .toast-stack {
      position: fixed;
      right: var(--space-4);
      bottom: var(--space-4);
      display: grid;
      gap: var(--space-3);
      z-index: var(--z-toast);
      width: min(360px, calc(100vw - 32px));
    }

    .toast {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: var(--space-3);
      padding: var(--space-4);
      border-radius: var(--radius-lg);
      border: 1px solid var(--color-border-strong);
      background: var(--color-surface-elevated);
      box-shadow: var(--shadow-lg);
      animation: slide-up 240ms ease;
    }

    .toast button {
      background: transparent;
      border: 0;
      color: inherit;
      font-size: 1.25rem;
      cursor: pointer;
    }

    .toast-error { border-color: rgba(232, 76, 61, 0.5); }
    .toast-success { border-color: rgba(76, 175, 130, 0.5); }
    .toast-warning { border-color: rgba(232, 168, 50, 0.5); }
    .toast-info { border-color: rgba(74, 144, 217, 0.5); }
  `]
})
export class NotificationToastComponent {
  protected readonly notificationService = inject(NotificationService);
}
