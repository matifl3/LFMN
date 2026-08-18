import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { InscripcionRequest, InscripcionResponse } from '../models/inscripcion.model';

@Injectable({ providedIn: 'root' })
export class InscripcionService {
  private readonly http = inject(HttpClient);

  inscribirse(request: InscripcionRequest) {
    return this.http.post<InscripcionResponse>('/api/inscripciones', request);
  }

  cancelarPorId(id: number) {
    return this.http.delete<InscripcionResponse>(`/api/inscripciones/${id}`);
  }

  cancelarPorCarreraYUsuario(carreraId: number, usuarioId: number) {
    return this.http.delete<InscripcionResponse>(
      `/api/inscripciones/carrera/${carreraId}/usuario/${usuarioId}`,
    );
  }

  deCarrera(carreraId: number) {
    return this.http.get<InscripcionResponse[]>(`/api/inscripciones/carrera/${carreraId}`);
  }

  deUsuario(usuarioId: number) {
    return this.http.get<InscripcionResponse[]>(`/api/inscripciones/usuario/${usuarioId}`);
  }

  contar(carreraId: number) {
    return this.http.get<{ inscriptos: number }>(`/api/inscripciones/carrera/${carreraId}/count`);
  }
}
