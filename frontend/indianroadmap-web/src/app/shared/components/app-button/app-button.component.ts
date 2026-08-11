import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button [attr.type]="type()" [disabled]="disabledState()" [ngClass]="classes()">
      @if (loading()) {
        <span class="spinner"></span>
      }
      <span><ng-content /></span>
    </button>
  `,
  styles: [`
    button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: var(--space-2);
      border-radius: var(--radius-full);
      border: 1px solid transparent;
      font-weight: 600;
      cursor: pointer;
      transition: transform var(--transition-fast), background var(--transition-fast), border-color var(--transition-fast);
    }

    button:hover:not(:disabled) {
      transform: translateY(-1px);
    }

    button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .primary { background: var(--color-accent); color: #fff; }
    .primary:hover:not(:disabled) { background: var(--color-accent-hover); }
    .secondary { background: transparent; color: var(--color-text-primary); border-color: var(--color-border-strong); }
    .ghost { background: transparent; color: var(--color-text-secondary); }

    .sm { padding: 0.55rem 0.9rem; font-size: 0.9rem; }
    .md { padding: 0.8rem 1.2rem; font-size: 0.95rem; }
    .lg { padding: 1rem 1.5rem; font-size: 1rem; }

    .spinner {
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.35);
      border-top-color: currentColor;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class AppButtonComponent {
  readonly variant = input<'primary' | 'secondary' | 'ghost'>('primary');
  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly loading = input(false);
  readonly disabled = input(false);
  readonly type = input<'button' | 'submit' | 'reset'>('button');

  readonly disabledState = computed(() => this.loading() || this.disabled());
  readonly classes = computed(() => `${this.variant()} ${this.size()}`);
}
