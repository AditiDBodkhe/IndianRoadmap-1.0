import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';
import { LoadingBarComponent } from '../navbar/loading-bar.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { NotificationToastComponent } from '../navbar/notification-toast.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, NotificationToastComponent, LoadingBarComponent],
  template: `
    <app-loading-bar />
    <app-navbar />
    <main class="main-content">
      <router-outlet />
    </main>
    <app-footer />
    <app-notification-toast />
  `,
  styles: [`
    .main-content {
      min-height: calc(100vh - 64px - 200px);
    }
  `]
})
export class AppShellComponent {}
