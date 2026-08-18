import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { ArchivoCarreraResponse } from '../models/archivo.model';
import { TipoArchivo } from '../models/archivo.model';

@Injectable({ providedIn: 'root' })
export class ArchivoService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<ArchivoCarreraResponse[]>('/api/archivos');
  }

  obtener(id: number) {
    return this.http.get<ArchivoCarreraResponse>(`/api/archivos/${id}`);
  }

  subir(nombre: string, tipo: TipoArchivo, archivo: File) {
    const formData = new FormData();
    formData.append('nombre', nombre);
    formData.append('tipo', tipo);
    formData.append('archivo', archivo);
    return this.http.post<ArchivoCarreraResponse>('/api/archivos', formData);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/archivos/${id}`);
  }

  urlDescarga(id: number): string {
    return `/api/archivos/${id}/descargar`;
  }
}
