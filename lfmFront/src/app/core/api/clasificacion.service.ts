import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { SesionClasificacionResponse } from '../models/clasificacion.model';

@Injectable({ providedIn: 'root' })
export class ClasificacionService {
  private readonly http = inject(HttpClient);

  deCarrera(carreraId: number) {
    return this.http.get<SesionClasificacionResponse[]>(
      `/api/clasificaciones/carrera/${carreraId}`,
    );
  }
}
