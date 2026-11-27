import { t } from '../../core/i18n/t';
import { Injectable, inject } from '@angular/core';
import { OperatorFunction, catchError, of } from 'rxjs';
import { apiError } from './api.service';
import { ToastService } from './toast.service';

/**
 * Reemplazo de `catchError(() => of([]))` para las cargas de paneles.
 *
 * Antes cualquier error se convertía en "no hay datos" sin dejar rastro: un 500
 * o un 403 se veían como una lista vacía (ver §4.4 de
 * `docs/arquitectura-y-patrones.md`). Ahora el panel sigue mostrando vacío,
 * pero el error ya no se pierde:
 *
 * - **Todos** quedan en la consola con la URL y el status.
 * - Los del servidor (status 0 o 5xx: caído, arrancando, error interno) avisan
 *   con **un** toast por carga, aunque fallen varios paneles a la vez.
 * - Los 4xx no muestran toast: varios son respuestas normales del negocio
 *   (404 "no hay resolución", 400 "no estás inscripto"), y avisarlos como error
 *   sería un bug nuevo.
 */
@Injectable({ providedIn: 'root' })
export class ErrorCargaService {
  private readonly toast = inject(ToastService);
  private avisoPendiente: unknown = null;

  /** Si la carga falla, registra el error y sigue con `valor` (lista vacía, null…). */
  siFalla<T, V>(valor: V): OperatorFunction<T, T | V> {
    return catchError((err: unknown) => {
      this.registrar(err);
      return of(valor);
    });
  }

  private registrar(err: unknown): void {
    const e = err as { status?: number; url?: string | null };
    console.error('[carga] ' + (e?.url ?? 'petición') + ' falló con status ' + e?.status, err);
    const status = e?.status;
    const delServidor = status === 0 || status === undefined || status >= 500;
    if (!delServidor || this.avisoPendiente) return;
    // Un solo toast por tanda: si el backend está caído fallan todos los
    // paneles de la página juntos y no tiene sentido mostrar uno por panel.
    this.avisoPendiente = err;
    setTimeout(() => {
      this.toast.error(t('comun.cargaParcial', { detalle: apiError(this.avisoPendiente) }));
      this.avisoPendiente = null;
    }, 300);
  }
}
