import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';

import { AuthService } from '../../core/auth/auth.service';
import { Rol } from '../../core/models/common.model';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
})
export class Layout {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly usuario = this.auth.usuario;
  readonly rol = this.auth.rol;

  readonly menuPrincipal: NavItem[] = [
    { path: '/', label: 'Inicio', icon: 'home' },
    { path: '/carreras', label: 'Carreras', icon: 'sports_motorsports' },
    { path: '/campeonatos', label: 'Campeonato', icon: 'emoji_events' },
    { path: '/categorias', label: 'Categorias', icon: 'category' },
    { path: '/setups', label: 'Setups', icon: 'settings' },
  ];

  readonly menuSesion: NavItem[] = [
    { path: '/perfil', label: 'Mi perfil', icon: 'person' },
    { path: '/notificaciones', label: 'Notificaciones', icon: 'notifications' },
    { path: '/logros', label: 'Logros', icon: 'military_tech' },
  ];

  readonly menuAdmin: NavItem[] = [
    { path: '/admin/usuarios', label: 'Usuarios', icon: 'group' },
    { path: '/admin/categorias', label: 'Categorias', icon: 'category' },
    { path: '/admin/carreras', label: 'Carreras', icon: 'sports_motorsports' },
    { path: '/admin/campeonatos', label: 'Campeonatos', icon: 'emoji_events' },
    { path: '/admin/logros', label: 'Logros', icon: 'military_tech' },
    { path: '/admin/anuncios', label: 'Anuncios', icon: 'campaign' },
    { path: '/admin/archivos', label: 'Archivos', icon: 'folder' },
    { path: '/admin/resultados', label: 'Resultados', icon: 'scoreboard' },
  ];

  get showAdmin(): boolean {
    return this.auth.hasRole([Rol.ADMIN]);
  }

  logout() {
    this.auth.logout();
    void this.router.navigate(['/']);
  }
}
