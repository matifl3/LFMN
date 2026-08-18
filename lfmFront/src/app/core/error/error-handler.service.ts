import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ApiError } from '../models/common.model';

const ERROR_AUTH = 'Debes iniciar sesion';
const ERROR_FORBIDDEN = 'No tienes permisos para esta accion';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
  private readonly snackBar = inject(MatSnackBar);

  handle(error: unknown, mensajePorDefecto = 'Ocurrio un error inesperado') {
    const mensaje = this.mensajeDe(error) ?? mensajePorDefecto;
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 5000,
      panelClass: 'lfm-snack-error',
    });
  }

  exito(mensaje: string) {
    this.snackBar.open(mensaje, 'Cerrar', {
      duration: 3000,
      panelClass: 'lfm-snack-success',
    });
  }

  mensajeDe(error: unknown): string | null {
    if (!(error instanceof HttpErrorResponse)) {
      return null;
    }
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor';
    }
    if (error.status === 401) {
      return ERROR_AUTH;
    }
    if (error.status === 403) {
      return ERROR_FORBIDDEN;
    }
    const body = error.error as Partial<ApiError> | null;
    if (body && typeof body.message === 'string' && body.message.length > 0) {
      return body.message;
    }
    return null;
  }
}
