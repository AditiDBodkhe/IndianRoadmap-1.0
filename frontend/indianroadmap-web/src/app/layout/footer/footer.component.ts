import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="footer">
      <div class="container footer-grid">
        <div>
          <h3>IndianRoadmap</h3>
          <p>Immersive Indian travel discovery and cultural storytelling for curious explorers.</p>
        </div>
        <div class="links">
          <a routerLink="/explore">Explore</a>
          <a routerLink="/recommendations">Recommendations</a>
          <a routerLink="/roadmaps">Roadmaps</a>
        </div>
      </div>
      <div class="footer-bottom">© 2026 IndianRoadmap. Crafted for mindful journeys.</div>
    </footer>
  `,
  styles: [`
    .footer {
      margin-top: var(--space-16);
      padding: var(--space-12) 0 var(--space-6);
      background: linear-gradient(180deg, rgba(19, 19, 26, 0.9), rgba(10, 10, 15, 1));
      border-top: 1px solid var(--color-border);
    }

    .footer-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: var(--space-8);
      align-items: start;
    }

    .footer h3 {
      margin-bottom: var(--space-3);
      font-family: var(--font-display);
    }

    .footer p,
    .footer-bottom,
    .links a {
      color: var(--color-text-secondary);
    }

    .links {
      display: grid;
      gap: var(--space-3);
      justify-content: end;
    }

    .footer-bottom {
      margin-top: var(--space-8);
      text-align: center;
      font-size: 0.9rem;
    }

    @media (max-width: 768px) {
      .footer-grid {
        grid-template-columns: 1fr;
      }

      .links {
        justify-content: start;
      }
    }
  `]
})
export class FooterComponent {}
