import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AuthStore } from '../../core/auth/auth.store';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <header class="navbar">
      <div class="container navbar-inner">
        <a class="brand" routerLink="/">IndianRoadmap</a>

        <button class="menu-toggle" type="button" (click)="toggleMenu()" [attr.aria-expanded]="menuOpen()">
          <span></span>
          <span></span>
          <span></span>
        </button>

        <nav class="nav-links" [class.open]="menuOpen()">
          <a routerLink="/explore" routerLinkActive="active" (click)="closeMenu()">Explore</a>
          <a routerLink="/roadmaps" routerLinkActive="active" (click)="closeMenu()">Roadmaps</a>
          <a routerLink="/recommendations" routerLinkActive="active" (click)="closeMenu()">Recommendations</a>

          @if (authStore.isAuthenticated()) {
            <a routerLink="/profile" routerLinkActive="active" (click)="closeMenu()">Profile</a>
            <button type="button" class="btn-ghost nav-action" (click)="logout()">Logout</button>
          } @else {
            <a routerLink="/login" routerLinkActive="active" (click)="closeMenu()">Login</a>
          }
        </nav>
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      position: sticky;
      top: 0;
      z-index: var(--z-navbar);
      border-bottom: 1px solid var(--color-border);
      background: rgba(10, 10, 15, 0.88);
      backdrop-filter: blur(18px);
    }

    .navbar-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 64px;
      gap: var(--space-4);
    }

    .brand {
      color: var(--color-text-primary);
      font-family: var(--font-display);
      font-size: 1.35rem;
      font-weight: 700;
      letter-spacing: 0.02em;
    }

    .nav-links {
      display: flex;
      align-items: center;
      gap: var(--space-4);
    }

    .nav-links a,
    .nav-action {
      color: var(--color-text-secondary);
      font-size: 0.95rem;
      transition: color var(--transition-fast);
    }

    .nav-links a:hover,
    .nav-links a.active,
    .nav-action:hover {
      color: var(--color-text-primary);
    }

    .menu-toggle {
      display: none;
      flex-direction: column;
      gap: 4px;
      background: transparent;
      border: 0;
      padding: var(--space-2);
    }

    .menu-toggle span {
      width: 22px;
      height: 2px;
      background: var(--color-text-primary);
      border-radius: var(--radius-full);
    }

    @media (max-width: 768px) {
      .menu-toggle {
        display: flex;
      }

      .nav-links {
        position: absolute;
        top: 64px;
        left: 0;
        right: 0;
        display: none;
        flex-direction: column;
        align-items: stretch;
        padding: var(--space-4);
        background: var(--color-surface);
        border-bottom: 1px solid var(--color-border);
      }

      .nav-links.open {
        display: flex;
      }

      .nav-links a,
      .nav-action {
        width: 100%;
        text-align: left;
      }
    }
  `]
})
export class NavbarComponent {
  protected readonly authStore = inject(AuthStore);
  protected readonly authService = inject(AuthService);
  protected readonly menuOpen = signal(false);
  protected readonly isAuthenticated = computed(() => this.authStore.isAuthenticated());

  logout(): void {
    this.authService.logout().subscribe();
    this.menuOpen.set(false);
  }

  toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }
}
