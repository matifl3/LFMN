import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { VueltaResponse } from '../models/vuelta.model';

@Injectable({ providedIn: 'root' })
export class VueltaService {
  private readonly http = inject(HttpClient);

  deCarrera(carreraId: number) {
    return this.http.get<VueltaResponse[]>(`/api/vueltas/carrera/${carreraId}`);
  }

  deCarreraYUsuario(carreraId: number, usuarioId: number) {
    return this.http.get<VueltaResponse[]>(
      `/api/vueltas/carrera/${carreraId}/usuario/${usuarioId}`,
    );
  }
}
