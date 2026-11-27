import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { LoginResponse, Usuario, Rol } from '../models/models';
import { ApiService } from './api.service';

const SESSION_KEY = 'lfm_session';

interface Sesion {
  token: string;
  usuario: Usuario | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  readonly user = signal<Usuario | null>(null);
  readonly token = signal<string | null>(null);
  readonly autenticado = computed(() => !!this.token() && !!this.user());

  constructor() {
    this.restaurarSesion();
  }

  private restaurarSesion(): void {
    try {
      const s = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null') as Sesion | null;
      if (s?.token && !jwtVencido(s.token)) {
        this.token.set(s.token);
        this.user.set(s.usuario);
      } else if (s) {
        localStorage.removeItem(SESSION_KEY);
      }
    } catch {
      localStorage.removeItem(SESSION_KEY);
    }
  }

  /**
   * Refresca el usuario guardado contra el backend: el rol y el nombre en localStorage
   * pueden estar viejos (ej. un admin le cambio el rol). Un 401 lo resuelve el interceptor
   * cerrando la sesion. Se llama desde App y no desde el constructor, para no crear una
   * dependencia circular AuthService -> HttpClient -> interceptor -> AuthService.
   */
  revalidar(): void {
    if (!this.token()) return;
    this.api.get<Usuario>('/usuarios/me').subscribe({
      next: (u) => this.updateUser(u),
      error: () => {
        // Sesion a medias (token sin usuario): no se puede usar, mejor limpiarla.
        if (!this.user()) this.clearSesion();
      },
    });
  }

  private persistir(): void {
    localStorage.setItem(
      SESSION_KEY,
      JSON.stringify({ token: this.token(), usuario: this.user() }),
    );
  }

  setSesion(token: string, usuario: Usuario | null): void {
    this.token.set(token);
    this.user.set(usuario);
    this.persistir();
  }

  updateUser(usuario: Usuario): void {
    this.user.set(usuario);
    this.persistir();
  }

  clearSesion(): void {
    this.token.set(null);
    this.user.set(null);
    localStorage.removeItem(SESSION_KEY);
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/usuarios/login', { email, password });
  }

  logout(): void {
    this.clearSesion();
  }

  hasRole(...roles: Rol[]): boolean {
    const u = this.user();
    return !!u && roles.includes(u.rol);
  }

  esAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  esAdminCampeonato(): boolean {
    return this.hasRole('ADMIN_CAMPEONATO');
  }

  esComisario(): boolean {
    return this.hasRole('COMISARIO');
  }

  /** Acceso a moderación / panel: ADMIN o COMISARIO */
  esModerador(): boolean {
    return this.esAdmin() || this.esComisario();
  }

  /** Puede entrar al panel de organisation de sus propios campeonatos. */
  esOrganizador(): boolean {
    return this.esAdmin() || this.esAdminCampeonato();
  }

  /** Redirect post-login corrige el bug del `next` roto del front viejo. */
  redirectPostLogin(): void {
    const next = localStorage.getItem('lfm_next');
    localStorage.removeItem('lfm_next');
    this.router.navigateByUrl(next && next.startsWith('/') && !next.startsWith('//') ? next : '/');
  }

  guardarNext(returnUrl: string): void {
    localStorage.setItem('lfm_next', returnUrl);
  }
}

/** true si el JWT tiene `exp` y ya paso. Sin `exp` legible lo decide el backend (401). */
function jwtVencido(token: string): boolean {
  try {
    const payload = token.split('.')[1];
    if (!payload) return false;
    const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    return typeof json.exp === 'number' && json.exp * 1000 <= Date.now();
  } catch {
    return false;
  }
}
