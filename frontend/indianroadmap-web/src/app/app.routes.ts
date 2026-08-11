import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home-page.component').then((m) => m.HomePageComponent) },
  { path: 'explore', loadComponent: () => import('./features/explore/explore-page.component').then((m) => m.ExplorePageComponent) },
  { path: 'destinations/:slug', loadComponent: () => import('./features/destination/destination-page.component').then((m) => m.DestinationPageComponent) },
  { path: 'roadmaps', loadComponent: () => import('./features/roadmap/roadmap-page.component').then((m) => m.RoadmapPageComponent), canActivate: [authGuard] },
  { path: 'roadmaps/:slug', loadComponent: () => import('./features/roadmap/roadmap-detail-page.component').then((m) => m.RoadmapDetailPageComponent), canActivate: [authGuard] },
  { path: 'recommendations', loadComponent: () => import('./features/recommendation/recommendation-page.component').then((m) => m.RecommendationPageComponent), canActivate: [authGuard] },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent) },
  { path: 'profile', loadComponent: () => import('./features/profile/profile-page.component').then((m) => m.ProfilePageComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
