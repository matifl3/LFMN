import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// `state.url` es la ruta pedida; `router.url` todavia es la anterior a la navegacion,
// y guardarla mandaba al usuario a la pagina vieja despues del login.

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.autenticado()) return true;
  auth.guardarNext(state.url);
  return router.createUrlTree(['/auth']);
};

export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    auth.guardarNext(state.url);
    return router.createUrlTree(['/auth']);
  }
  if (auth.esModerador()) return true;
  return router.createUrlTree(['/']);
};

/** ADMIN global o ADMIN_CAMPEONATO: administran sus propios campeonatos privados. */
export const organizadorGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    auth.guardarNext(state.url);
    return router.createUrlTree(['/auth']);
  }
  if (auth.esOrganizador()) return true;
  return router.createUrlTree(['/']);
};
