import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import {
  CampeonatoRequest,
  CampeonatoResponse,
  TablaPosicionResponse,
} from '../models/campeonato.model';

@Injectable({ providedIn: 'root' })
export class CampeonatoService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<CampeonatoResponse[]>('/api/campeonatos');
  }

  porCategoria(categoriaId: number) {
    return this.http.get<CampeonatoResponse[]>(`/api/campeonatos/categoria/${categoriaId}`);
  }

  obtener(id: number) {
    return this.http.get<CampeonatoResponse>(`/api/campeonatos/${id}`);
  }

  tabla(id: number) {
    return this.http.get<TablaPosicionResponse[]>(`/api/campeonatos/${id}/tabla`);
  }

  crear(request: CampeonatoRequest) {
    return this.http.post<CampeonatoResponse>('/api/campeonatos', request);
  }

  actualizar(id: number, request: CampeonatoRequest) {
    return this.http.put<CampeonatoResponse>(`/api/campeonatos/${id}`, request);
  }

  cerrar(id: number) {
    return this.http.put<CampeonatoResponse>(`/api/campeonatos/${id}/cerrar`, null);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/campeonatos/${id}`);
  }
}
