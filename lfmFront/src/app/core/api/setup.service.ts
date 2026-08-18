import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import {
  SetupCalificacionRequest,
  SetupCalificacionResponse,
  SetupComentarioRequest,
  SetupComentarioResponse,
  SetupRequest,
  SetupResponse,
} from '../models/setup.model';

@Injectable({ providedIn: 'root' })
export class SetupService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<SetupResponse[]>('/api/setups');
  }

  buscar(circuito?: string, vehiculo?: string) {
    let params = new HttpParams();
    if (circuito) {
      params = params.set('circuito', circuito);
    }
    if (vehiculo) {
      params = params.set('vehiculo', vehiculo);
    }
    return this.http.get<SetupResponse[]>('/api/setups/buscar', { params });
  }

  porAutor(autorId: number) {
    return this.http.get<SetupResponse[]>(`/api/setups/autor/${autorId}`);
  }

  porCategoria(categoriaId: number) {
    return this.http.get<SetupResponse[]>(`/api/setups/categoria/${categoriaId}`);
  }

  obtener(id: number) {
    return this.http.get<SetupResponse>(`/api/setups/${id}`);
  }

  crear(request: SetupRequest) {
    return this.http.post<SetupResponse>('/api/setups', request);
  }

  actualizar(id: number, request: SetupRequest) {
    return this.http.put<SetupResponse>(`/api/setups/${id}`, request);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/setups/${id}`);
  }

  comentarios(id: number) {
    return this.http.get<SetupComentarioResponse[]>(`/api/setups/${id}/comentarios`);
  }

  agregarComentario(id: number, request: SetupComentarioRequest) {
    return this.http.post<SetupComentarioResponse>(`/api/setups/${id}/comentarios`, request);
  }

  calificar(id: number, request: SetupCalificacionRequest) {
    return this.http.post<SetupCalificacionResponse>(`/api/setups/${id}/calificaciones`, request);
  }
}
