import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { AnuncioRequest, AnuncioResponse } from '../models/anuncio.model';

@Injectable({ providedIn: 'root' })
export class AnuncioService {
  private readonly http = inject(HttpClient);

  listar() {
    return this.http.get<AnuncioResponse[]>('/api/anuncios');
  }

  ultimo() {
    return this.http.get<AnuncioResponse>('/api/anuncios/ultimo');
  }

  obtener(id: number) {
    return this.http.get<AnuncioResponse>(`/api/anuncios/${id}`);
  }

  crear(request: AnuncioRequest) {
    return this.http.post<AnuncioResponse>('/api/anuncios', request);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`/api/anuncios/${id}`);
  }
}
