import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Endpoints de autenticacion: van sin JWT y un 401 suyo (credenciales malas) no cierra sesion.
 * Lista exacta, no `includes('/steam')`: eso dejaba sin token a /steam/vincular-url y
 * DELETE /usuarios/{id}/steam, que SI requieren usuario autenticado.
 */
const RUTAS_AUTH = [
  '/api/usuarios/login',
  '/api/usuarios/registro-steam',
  '/api/usuarios/vincular-steam-login',
  '/api/steam/auth-url',
  '/api/steam/completar',
  '/api/recuperar/solicitar',
  '/api/recuperar/restablecer',
];

function esRutaAuth(req: HttpRequest<unknown>): boolean {
  const path = req.url.split('?')[0];
  if (RUTAS_AUTH.some((r) => path.endsWith(r))) return true;
  // Registro por email es POST /api/usuarios; el GET (listado admin) si lleva token.
  return req.method === 'POST' && path.endsWith('/api/usuarios');
}

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esAuthEndpoint = esRutaAuth(req);

  let r = req;
  const token = auth.token();
  if (token && !esAuthEndpoint) {
    r = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(r).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !esAuthEndpoint && auth.token()) {
        auth.clearSesion();
        router.navigate(['/auth'], { queryParams: { next: router.url } });
      }
      return throwError(() => err);
    }),
  );
};
