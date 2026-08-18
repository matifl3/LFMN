import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { CategoriaRequest, CategoriaResponse } from '../models/categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<CategoriaResponse[]>('/api/categorias');
  }

  disponibles(elo: number) {
    return this.http.get<CategoriaResponse[]>('/api/categorias/disponibles', {
      params: { elo },
    });
  }

  obtener(id: number) {
    return this.http.get<CategoriaResponse>(`/api/categorias/${id}`);
  }

  crear(request: CategoriaRequest) {
    return this.http.post<CategoriaResponse>('/api/categorias', request);
  }

  actualizar(id: number, request: CategoriaRequest) {
    return this.http.put<CategoriaResponse>(`/api/categorias/${id}`, request);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/categorias/${id}`);
  }
}
