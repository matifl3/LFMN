import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esAuthEndpoint = req.url.includes('/login') || req.url.includes('/registro') || req.url.includes('/steam');

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
    })
  );
};