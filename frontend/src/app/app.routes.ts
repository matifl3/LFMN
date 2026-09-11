import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent) },
  { path: 'auth', loadComponent: () => import('./features/auth/auth.component').then((m) => m.AuthComponent) },
  { path: 'carreras', loadComponent: () => import('./features/races/races-list/races-list.component').then((m) => m.RacesListComponent) },
  { path: 'carreras/:id', loadComponent: () => import('./features/races/race-detail/race-detail.component').then((m) => m.RaceDetailComponent) },
  { path: 'campeonato', loadComponent: () => import('./features/championship/championship.component').then((m) => m.ChampionshipComponent) },
  { path: 'categorias', loadComponent: () => import('./features/categories/categories.component').then((m) => m.CategoriesComponent) },
  { path: 'setups', loadComponent: () => import('./features/setups/setups.component').then((m) => m.SetupsComponent) },
  { path: 'perfil/:id', loadComponent: () => import('./features/profile/driver-profile/driver-profile.component').then((m) => m.DriverProfileComponent) },
  { path: 'mi-perfil', canActivate: [authGuard], loadComponent: () => import('./features/profile/my-profile/my-profile.component').then((m) => m.MyProfileComponent) },
  { path: 'notificaciones', canActivate: [authGuard], loadComponent: () => import('./features/notifications/notifications.component').then((m) => m.NotificationsComponent) },
  { path: 'logros', loadComponent: () => import('./features/achievements/achievements.component').then((m) => m.AchievementsComponent) },
  { path: 'incidentes', loadComponent: () => import('./features/incidents/incidents.component').then((m) => m.IncidentsComponent) },
  { path: 'admin', canActivate: [adminGuard], loadComponent: () => import('./features/admin/admin.component').then((m) => m.AdminComponent) },
  { path: '**', redirectTo: '' },
];