import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { IncidenteResponse } from '../models/incidente.model';

@Injectable({ providedIn: 'root' })
export class IncidenteService {
  private readonly http = inject(HttpClient);

  deCarrera(carreraId: number) {
    return this.http.get<IncidenteResponse[]>(
      `/api/incidentes/carrera/${carreraId}`,
    );
  }
}
