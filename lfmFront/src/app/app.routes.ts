import { Routes } from '@angular/router';

import { Layout } from './shared/layout/layout.component';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { Rol } from './core/models/common.model';

const adminGuard = roleGuard([Rol.ADMIN]);

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'registro',
        loadComponent: () => import('./features/auth/registro.component').then((m) => m.RegistroComponent),
      },
      {
        path: 'carreras',
        loadComponent: () =>
          import('./features/carreras/carreras-list.component').then((m) => m.CarrerasListComponent),
      },
      {
        path: 'carreras/:id',
        loadComponent: () =>
          import('./features/carreras/carrera-detail.component').then((m) => m.CarreraDetailComponent),
      },
      {
        path: 'campeonatos',
        loadComponent: () =>
          import('./features/campeonato/campeonato.component').then((m) => m.CampeonatoComponent),
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/categorias/categorias.component').then((m) => m.CategoriasComponent),
      },
      {
        path: 'setups',
        loadComponent: () =>
          import('./features/setups/setups-list.component').then((m) => m.SetupsListComponent),
      },
      {
        path: 'setups/:id',
        loadComponent: () =>
          import('./features/setups/setup-detail.component').then((m) => m.SetupDetailComponent),
      },
      {
        path: 'pilotos/:id',
        loadComponent: () =>
          import('./features/piloto/piloto-publico.component').then((m) => m.PilotoPublicoComponent),
      },
      {
        path: 'logros',
        loadComponent: () =>
          import('./features/logros/logros.component').then((m) => m.LogrosComponent),
      },
      {
        path: 'perfil',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/perfil/perfil.component').then((m) => m.PerfilComponent),
      },
      {
        path: 'notificaciones',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/notificaciones/notificaciones.component').then((m) => m.NotificacionesComponent),
      },
      {
        path: 'admin/usuarios',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-usuarios.component').then((m) => m.AdminUsuariosComponent),
      },
      {
        path: 'admin/categorias',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-categorias.component').then((m) => m.AdminCategoriasComponent),
      },
      {
        path: 'admin/carreras',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-carreras.component').then((m) => m.AdminCarrerasComponent),
      },
      {
        path: 'admin/campeonatos',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-campeonatos.component').then((m) => m.AdminCampeonatosComponent),
      },
      {
        path: 'admin/logros',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-logros.component').then((m) => m.AdminLogrosComponent),
      },
      {
        path: 'admin/anuncios',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-anuncios.component').then((m) => m.AdminAnunciosComponent),
      },
      {
        path: 'admin/archivos',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-archivos.component').then((m) => m.AdminArchivosComponent),
      },
      {
        path: 'admin/resultados',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/admin-resultados.component').then((m) => m.AdminResultadosComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
