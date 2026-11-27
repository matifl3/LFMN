import { signal } from '@angular/core';
import { translate } from '@jsverse/transloco';

/** Idioma activo como signal: lo actualiza IdiomaService al cambiar. */
export const idiomaActivo = signal<string>('es');

/**
 * Traduce desde TypeScript (toasts, labels calculados, etc.).
 *
 * Lee `idiomaActivo` para que un `computed()` o un template que la use se
 * recalcule al cambiar de idioma; `translate()` de Transloco solo no avisa.
 */
export function t(clave: string, params?: Record<string, unknown>): string {
  idiomaActivo();
  return translate(clave, params);
}
