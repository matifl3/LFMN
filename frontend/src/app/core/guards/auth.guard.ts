import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.autenticado()) return true;
  auth.guardarNext(router.url);
  return router.createUrlTree(['/auth']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    auth.guardarNext(router.url);
    return router.createUrlTree(['/auth']);
  }
  if (auth.esModerador()) return true;
  return router.createUrlTree(['/']);
};