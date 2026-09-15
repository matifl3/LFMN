import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { normalizeList } from '../utils/formato';

export interface ApiOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  body?: unknown;
  params?: Record<string, string | number | boolean | null | undefined>;
  /** true by default: agrega Authorization. false para endpoints públicos (steam auth-url). */
  auth?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  private url(path: string): string {
    return `${environment.apiUrl}/api${path.startsWith('/') ? '' : '/'}${path}`;
  }

  private params(obj?: Record<string, string | number | boolean | null | undefined>): HttpParams | undefined {
    if (!obj) return undefined;
    let p = new HttpParams();
    for (const k of Object.keys(obj)) {
      const v = obj[k];
      if (v === null || v === undefined || v === '') continue;
      p = p.set(k, String(v));
    }
    return p;
  }

  api<T>(path: string, options: ApiOptions = {}): Observable<T> {
    const method = options.method || 'GET';
    const body = options.body !== undefined ? options.body : undefined;
    return this.http
      .request<T>(method, this.url(path), {
        body,
        params: this.params(options.params),
        responseType: 'json',
      })
      .pipe(
        map((res) => {
          if (res === null || res === undefined) return res as T;
          return res as T;
        })
      );
  }

  get<T>(path: string, params?: ApiOptions['params']): Observable<T> {
    return this.api<T>(path, { method: 'GET', params });
  }

  post<T>(path: string, body?: unknown): Observable<T> {
    return this.api<T>(path, { method: 'POST', body });
  }

  put<T>(path: string, body?: unknown): Observable<T> {
    return this.api<T>(path, { method: 'PUT', body });
  }

  del<T>(path: string): Observable<T> {
    return this.api<T>(path, { method: 'DELETE' });
  }

  patch<T>(path: string, body?: unknown): Observable<T> {
    return this.api<T>(path, { method: 'PATCH', body });
  }

  /** Lista tipada tolerando paginación inconsistente ({content} || array) */
  list<T>(path: string, params?: ApiOptions['params']): Observable<T[]> {
    return this.get<unknown>(path, params).pipe(map((r) => normalizeList<T>(r)));
  }
}

const MENSAJES_HTTP: Record<number, string> = {
  400: 'La solicitud es inválida. Revisá los datos ingresados.',
  401: 'Tu sesión venció. Volvé a iniciar sesión.',
  403: 'No tenés permisos para realizar esta acción.',
  404: 'No se encontró el recurso solicitado.',
  500: 'Ocurrió un error interno en el servidor.',
  502: 'El servidor está temporalmente fuera de servicio. Volvé a intentarlo en unos minutos.',
  503: 'El servidor está sobrecargado o en mantenimiento. Volvé a intentarlo en un momento.',
  504: 'El servidor se demoró en responder. Volvé a intentarlo en unos minutos.',
};

export function apiError(err: unknown): string {
  const e = err as { error?: { mensaje?: string; error?: string; message?: string } | string; status?: number };
  const body = e?.error;
  if (body && typeof body === 'object') {
    const msg = body.mensaje || body.error || body.message;
    if (msg) return msg;
  }
  const status = e?.status;
  if (status && MENSAJES_HTTP[status]) return MENSAJES_HTTP[status];
  if (status) return 'Error ' + status;
  return 'No se pudo conectar con el servidor. ¿Está levantado el backend en :8080?';
}