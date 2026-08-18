import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { LoginRequest, LoginResponse, UsuarioRequest, UsuarioResponse } from '../models/usuario.model';
import { Rol } from '../models/common.model';

const TOKEN_KEY = 'lfm_token';
const USUARIO_KEY = 'lfm_usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly usuarioSignal = signal<UsuarioResponse | null>(this.loadUsuario());

  readonly token = this.tokenSignal.asReadonly();
  readonly usuario = this.usuarioSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.tokenSignal() !== null);
  readonly rol = computed(() => this.usuarioSignal()?.rol ?? null);

  login(loginRequest: LoginRequest) {
    return this.http.post<LoginResponse>('/api/usuarios/login', loginRequest);
  }

  registrar(usuarioRequest: UsuarioRequest) {
    return this.http.post<UsuarioResponse>('/api/usuarios/registro', usuarioRequest);
  }

  guardarSesion(token: string, usuario: UsuarioResponse) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
    this.tokenSignal.set(token);
    this.usuarioSignal.set(usuario);
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
    this.tokenSignal.set(null);
    this.usuarioSignal.set(null);
  }

  hasRole(roles: Rol[]): boolean {
    const rol = this.usuarioSignal()?.rol;
    return rol !== null && rol !== undefined && roles.includes(rol);
  }

  private loadUsuario(): UsuarioResponse | null {
    const raw = localStorage.getItem(USUARIO_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as UsuarioResponse;
    } catch {
      return null;
    }
  }
}
