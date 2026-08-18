import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { CargarResultadosRequest, ResultadoCarreraResponse } from '../models/resultado.model';

@Injectable({ providedIn: 'root' })
export class ResultadoService {
  private readonly http = inject(HttpClient);

  deCarrera(carreraId: number) {
    return this.http.get<ResultadoCarreraResponse[]>(`/api/resultados/carrera/${carreraId}`);
  }

  deUsuario(usuarioId: number) {
    return this.http.get<ResultadoCarreraResponse[]>(`/api/resultados/usuario/${usuarioId}`);
  }

  obtener(id: number) {
    return this.http.get<ResultadoCarreraResponse>(`/api/resultados/${id}`);
  }

  cargar(request: CargarResultadosRequest) {
    return this.http.post<ResultadoCarreraResponse[]>('/api/resultados/cargar', request);
  }
}
