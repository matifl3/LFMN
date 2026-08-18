import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import {
  LogroRequest,
  LogroResponse,
  RecompensaRequest,
  RecompensaResponse,
  UsuarioLogroResponse,
  UsuarioRecompensaResponse,
} from '../models/logro.model';

@Injectable({ providedIn: 'root' })
export class LogroService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<LogroResponse[]>('/api/logros');
  }

  obtener(id: number) {
    return this.http.get<LogroResponse>(`/api/logros/${id}`);
  }

  crear(request: LogroRequest) {
    return this.http.post<LogroResponse>('/api/logros', request);
  }

  actualizar(id: number, request: LogroRequest) {
    return this.http.put<LogroResponse>(`/api/logros/${id}`, request);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/logros/${id}`);
  }

  agregarRecompensa(logroId: number, request: RecompensaRequest) {
    return this.http.post<RecompensaResponse>(`/api/logros/${logroId}/recompensas`, request);
  }

  quitarRecompensa(logroId: number, recompensaId: number) {
    return this.http.delete<void>(`/api/logros/${logroId}/recompensas/${recompensaId}`);
  }

  listarRecompensas() {
    return this.http.get<RecompensaResponse[]>('/api/recompensas');
  }

  progresoPorUsuario(usuarioId: number) {
    return this.http.get<UsuarioLogroResponse[]>(`/api/usuarios/${usuarioId}/logros`);
  }

  obtenidosPorUsuario(usuarioId: number) {
    return this.http.get<UsuarioLogroResponse[]>(`/api/usuarios/${usuarioId}/logros/obtenidos`);
  }

  recompensasPorUsuario(usuarioId: number) {
    return this.http.get<UsuarioRecompensaResponse[]>(`/api/usuarios/${usuarioId}/recompensas`);
  }

  recompensasNoReclamadasPorUsuario(usuarioId: number) {
    return this.http.get<UsuarioRecompensaResponse[]>(`/api/usuarios/${usuarioId}/recompensas/no-reclamadas`);
  }

  reclamarRecompensa(usuarioId: number, recompensaId: number) {
    return this.http.post<void>(
      `/api/recompensas/usuario/${usuarioId}/recompensas/${recompensaId}/reclamar`,
      null,
    );
  }
}
