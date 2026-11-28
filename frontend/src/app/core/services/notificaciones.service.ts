import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';

/**
 * Contador de notificaciones sin leer, compartido entre la campanita del header y
 * la pantalla de notificaciones. Antes el header guardaba su propia copia y solo la
 * pedía al iniciar sesión: marcar todas como leídas (o una, o eliminar) no la
 * actualizaba hasta recargar la página.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionesService {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  private readonly _noLeidas = signal(0);
  readonly noLeidas = this._noLeidas.asReadonly();

  /** Vuelve a pedir el contador al backend (fuente de verdad). */
  refrescar(): void {
    if (!this.auth.autenticado()) {
      this._noLeidas.set(0);
      return;
    }
    this.api.get<number>('/notificaciones/me/no-leidas/contar').subscribe({
      next: (n) => this._noLeidas.set(n > 0 ? n : 0),
      error: () => this._noLeidas.set(0),
    });
  }

  /** Al cerrar sesión. */
  limpiar(): void {
    this._noLeidas.set(0);
  }
}
