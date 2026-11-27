import { Injectable, signal } from '@angular/core';

/**
 * Reloj compartido para las cuentas regresivas ("Faltan 2 d 4 h"). Un único
 * intervalo para toda la app en vez de uno por componente; la resolución de
 * 30 s alcanza porque la cuenta muestra minutos como unidad mínima.
 */
@Injectable({ providedIn: 'root' })
export class RelojService {
  private readonly _ahora = signal(Date.now());
  readonly ahora = this._ahora.asReadonly();

  constructor() {
    setInterval(() => this._ahora.set(Date.now()), 30_000);
  }
}
