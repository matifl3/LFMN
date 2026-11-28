import { Injectable } from '@angular/core';
import { Translation, TranslocoLoader } from '@jsverse/transloco';

/**
 * Las traducciones van en el bundle como módulos TS (import dinámico, un chunk
 * por idioma) y no como JSON pedido por HTTP: así quedan tipadas y no dependen
 * de la CSP ni del interceptor de auth.
 */
@Injectable({ providedIn: 'root' })
export class TranslocoTsLoader implements TranslocoLoader {
  getTranslation(lang: string): Promise<Translation> {
    return lang === 'en' ? import('./en').then((m) => m.en) : import('./es').then((m) => m.es);
  }
}
