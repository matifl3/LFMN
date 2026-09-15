import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, throwError, timer } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { normalizeList } from '../utils/formato';

export interface ApiOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  body?: unknown;
  params?: Record<string, string | number | boolean | null | undefined>;
  /** true by default: agrega Authorization. false para endpoints públicos (steam auth-url). */
  auth?: boolean;
}

/** Demoras (ms) entre reintentos ante errores transitorios de cold-start. */
const REINTENTOS_MS = [5000, 15000];

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
    const ejecutar = () =>
      this.http
        .request<T>(method, this.url(path), {
          body,
          params: this.params(options.params),
          responseType: 'json',
        })
        .pipe(map((res) => (res === null || res === undefined ? (res as T) : (res as T))));
    return this.reintentar(ejecutar, 0);
  }

  /** Reintenta errores transitorios (cold start de Render: 502/503/504, red, o HTML). */
  private reintentar<T>(ejecutar: () => Observable<T>, intento: number): Observable<T> {
    return ejecutar().pipe(
      catchError((err) => {
        if (esErrorTransitorio(err) && intento < REINTENTOS_MS.length) {
          return timer(REINTENTOS_MS[intento]).pipe(mergeMap(() => this.reintentar(ejecutar, intento + 1)));
        }
        return throwError(() => err);
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
};

function esErrorTransitorio(err: HttpErrorResponse): boolean {
  if (err.status === 0) return true;
  if (err.status === 502 || err.status === 503 || err.status === 504) return true;
  const texto = typeof err.error === 'string' ? err.error : (err.error?.text as string | undefined) || '';
  return texto.startsWith('<');
}

export function apiError(err: unknown): string {
  const e = err as { error?: { mensaje?: string; error?: string; message?: string } | string; status?: number };
  if (e && (e.status === 0 || e.status === 502 || e.status === 503 || e.status === 504)) {
    return 'El servidor está arrancando. Reintentá en unos segundos.';
  }
  const body = e?.error;
  if (typeof body === 'string' && body.startsWith('<')) {
    return 'El servidor respondió inesperadamente. Reintentá en unos segundos.';
  }
  if (body && typeof body === 'object') {
    const msg = body.mensaje || body.error || body.message;
    if (msg) return msg;
  }
  const status = e?.status;
  if (status && MENSAJES_HTTP[status]) return MENSAJES_HTTP[status];
  if (status) return 'Error ' + status;
  return 'No se pudo conectar con el servidor. ¿Está levantado el backend?';
}