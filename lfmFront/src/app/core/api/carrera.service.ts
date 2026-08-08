import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { CarreraRequest, CarreraResponse, VincularArchivoRequest } from '../models/carrera.model';
import { EstadoCarrera } from '../models/carrera.model';

@Injectable({ providedIn: 'root' })
export class CarreraService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<CarreraResponse[]>('/api/carreras');
  }

  proximas() {
    return this.http.get<CarreraResponse[]>('/api/carreras/proximas');
  }

  pasadas() {
    return this.http.get<CarreraResponse[]>('/api/carreras/pasadas');
  }

  porCategoria(categoriaId: number) {
    return this.http.get<CarreraResponse[]>(`/api/carreras/categoria/${categoriaId}`);
  }

  obtener(id: number) {
    return this.http.get<CarreraResponse>(`/api/carreras/${id}`);
  }

  crear(request: CarreraRequest) {
    return this.http.post<CarreraResponse>('/api/carreras', request);
  }

  actualizar(id: number, request: CarreraRequest) {
    return this.http.put<CarreraResponse>(`/api/carreras/${id}`, request);
  }

  cambiarEstado(id: number, estado: EstadoCarrera) {
    return this.http.put<CarreraResponse>(`/api/carreras/${id}/estado`, null, {
      params: { estado },
    });
  }

  cancelar(id: number) {
    return this.http.put<CarreraResponse>(`/api/carreras/${id}/cancelar`, null);
  }

  vincularArchivo(id: number, request: VincularArchivoRequest) {
    return this.http.put<CarreraResponse>(`/api/carreras/${id}/archivo`, request);
  }

  desvincularArchivo(id: number) {
    return this.http.delete<CarreraResponse>(`/api/carreras/${id}/archivo`);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/carreras/${id}`);
  }
}
