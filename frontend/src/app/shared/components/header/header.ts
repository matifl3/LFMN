import {
  Component,
  ElementRef,
  HostListener,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { NavigationStart, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { ApiService, apiError } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../avatar/avatar';

export interface NavLink {
  path: string;
  label: string;
}

// Secciones públicas de la liga. "Inicio" no está: el logo ya lleva a "/".
// Lo personal (perfil, mis campeonatos, admin) vive en el menú del avatar.
const NAV_LINKS: NavLink[] = [
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
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly navLinks = NAV_LINKS;
  readonly noLeidas = signal(0);
  readonly menuAbierto = signal(false);
  readonly menuUsuarioAbierto = signal(false);
  readonly miPerfil = computed(() => {
    const id = this.auth.user()?.id;
    return id ? ['/perfil', id] : ['/mi-perfil'];
  });

  constructor() {
    effect(() => {
      if (this.auth.autenticado()) {
        this.cargarNoLeidas();
      } else {
        this.noLeidas.set(0);
        this.menuAbierto.set(false);
        this.menuUsuarioAbierto.set(false);
      }
    });

    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationStart),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.menuUsuarioAbierto.set(false));
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

  alternarMenuUsuario(): void {
    this.menuUsuarioAbierto.update((v) => !v);
  }

  @HostListener('document:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if (e.key !== 'Escape') return;
    if (this.menuAbierto()) this.menuAbierto.set(false);
    if (this.menuUsuarioAbierto()) {
      this.menuUsuarioAbierto.set(false);
      this.host.nativeElement.querySelector<HTMLElement>('.user-menu-toggle')?.focus();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(e: MouseEvent): void {
    if (!this.menuUsuarioAbierto()) return;
    const menu = this.host.nativeElement.querySelector('.user-menu');
    if (menu && !menu.contains(e.target as Node)) this.menuUsuarioAbierto.set(false);
  }

  salir(): void {
    this.auth.logout();
    this.menuAbierto.set(false);
    this.menuUsuarioAbierto.set(false);
    this.toast.success('Sesión cerrada');
    this.router.navigate(['/']);
  }
}
