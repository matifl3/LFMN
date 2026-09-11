import { Component, effect, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService, apiError } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../avatar/avatar';

export interface NavLink {
  path: string;
  label: string;
}

const BASE_LINKS: NavLink[] = [
  { path: '/', label: 'Inicio' },
  { path: '/carreras', label: 'Carreras' },
  { path: '/campeonato', label: 'Campeonato' },
  { path: '/categorias', label: 'Categorías' },
  { path: '/incidentes', label: 'Incidentes' },
  { path: '/setups', label: 'Setups' },
  { path: '/logros', label: 'Logros' },
];

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, Avatar],
  styleUrl: './header.scss',
  templateUrl: './header.html',
})
export class Header {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly navLinks = signal<NavLink[]>(BASE_LINKS);
  readonly noLeidas = signal(0);
  readonly menuAbierto = signal(false);

  constructor() {
    effect(() => {
      if (this.auth.autenticado()) {
        const links = [...BASE_LINKS];
        if (this.auth.esModerador()) links.push({ path: '/admin', label: 'Admin' });
        this.navLinks.set(links);
        this.cargarNoLeidas();
      } else {
        this.navLinks.set(BASE_LINKS);
        this.noLeidas.set(0);
        this.menuAbierto.set(false);
      }
    });
  }

  cargarNoLeidas(): void {
    if (!this.auth.autenticado()) return;
    this.api.get<number>('/notificaciones/me/no-leidas/contar').subscribe({
      next: (n) => this.noLeidas.set(n > 0 ? n : 0),
      error: () => this.noLeidas.set(0),
    });
  }

  alternarMenu(): void {
    this.menuAbierto.update((v) => !v);
  }

  salir(): void {
    this.auth.logout();
    this.menuAbierto.set(false);
    this.toast.success('Sesión cerrada');
    this.router.navigate(['/']);
  }
}