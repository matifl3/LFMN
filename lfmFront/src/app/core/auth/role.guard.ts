import { inject } from '@angular/core';
import { Router } from '@angular/router';

import { Rol } from '../models/common.model';
import { AuthService } from './auth.service';

export const roleGuard = (roles: Rol[]) => () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.hasRole(roles)) {
    return true;
  }
  return router.createUrlTree(['/']);
};
