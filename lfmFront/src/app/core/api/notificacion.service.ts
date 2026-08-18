import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { NotificacionResponse } from '../models/notificacion.model';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);

  porUsuario(usuarioId: number) {
    return this.http.get<NotificacionResponse[]>(`/api/notificaciones/usuario/${usuarioId}`);
  }

  noLeidas(usuarioId: number) {
    return this.http.get<NotificacionResponse[]>(`/api/notificaciones/usuario/${usuarioId}/no-leidas`);
  }

  contarNoLeidas(usuarioId: number) {
    return this.http.get<number>(`/api/notificaciones/usuario/${usuarioId}/no-leidas/contar`);
  }

  marcarLeida(id: number) {
    return this.http.put<NotificacionResponse>(`/api/notificaciones/${id}/leida`, null);
  }

  marcarTodasLeidas(usuarioId: number) {
    return this.http.put<void>(`/api/notificaciones/usuario/${usuarioId}/leidas`, null);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/notificaciones/${id}`);
  }
}
