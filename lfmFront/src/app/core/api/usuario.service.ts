import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import {
  CambioPasswordRequest,
  EloHistorialResponse,
  SafetyRatingHistorialResponse,
  StatsResponse,
  SteamRequest,
  UsuarioRequest,
  UsuarioResponse,
} from '../models/usuario.model';
import { Rol } from '../models/common.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<UsuarioResponse[]>('/api/usuarios');
  }

  obtener(id: number) {
    return this.http.get<UsuarioResponse>(`/api/usuarios/${id}`);
  }

  stats(id: number) {
    return this.http.get<StatsResponse>(`/api/usuarios/${id}/stats`);
  }

  historialElo(id: number) {
    return this.http.get<EloHistorialResponse[]>(`/api/usuarios/${id}/historial-elo`);
  }

  historialSafetyRating(id: number) {
    return this.http.get<SafetyRatingHistorialResponse[]>(`/api/usuarios/${id}/historial-safety-rating`);
  }

  actualizarPerfil(id: number, request: Partial<UsuarioRequest>) {
    return this.http.put<UsuarioResponse>(`/api/usuarios/${id}/perfil`, request);
  }

  cambiarPassword(id: number, request: CambioPasswordRequest) {
    return this.http.put<void>(`/api/usuarios/${id}/password`, request);
  }

  vincularSteam(id: number, request: SteamRequest) {
    return this.http.put<UsuarioResponse>(`/api/usuarios/${id}/steam`, request);
  }

  desvincularSteam(id: number) {
    return this.http.delete<UsuarioResponse>(`/api/usuarios/${id}/steam`);
  }

  cambiarRol(id: number, rol: Rol) {
    return this.http.put<UsuarioResponse>(`/api/usuarios/${id}/rol`, null, {
      params: { rol },
    });
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/usuarios/${id}`);
  }
}
