import { DOCUMENT, Injectable, inject, signal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { firstValueFrom, take } from 'rxjs';
import { idiomaActivo } from '../i18n/t';

export type Idioma = 'es' | 'en';

export const IDIOMAS: readonly Idioma[] = ['es', 'en'];

const STORAGE_KEY = 'lfm_idioma';

/**
 * Idioma elegido por el usuario. Se guarda en localStorage (no en el backend)
 * para que funcione también sin sesión; la primera vez se toma del navegador.
 */
@Injectable({ providedIn: 'root' })
export class IdiomaService {
  private readonly transloco = inject(TranslocoService);
  private readonly document = inject(DOCUMENT);

  readonly actual = signal<Idioma>('es');

  /** Se llama desde un app initializer: carga el idioma antes del primer render. */
  async iniciar(): Promise<void> {
    const idioma = this.inicial();
    await firstValueFrom(this.transloco.load(idioma));
    this.aplicar(idioma);
  }

  cambiar(idioma: Idioma): void {
    if (idioma === this.actual()) return;
    try {
      localStorage.setItem(STORAGE_KEY, idioma);
    } catch {
      // Sin storage (modo privado estricto): el cambio vale para esta pestaña.
    }
    this.aplicar(idioma);
  }

  private aplicar(idioma: Idioma): void {
    this.actual.set(idioma);
    this.transloco.setActiveLang(idioma);
    idiomaActivo.set(idioma);
    this.document.documentElement.lang = idioma;
    // selectTranslate espera a que cargue el idioma (translate() daría el de respaldo).
    this.transloco
      .selectTranslate<string>('idioma.tituloPagina', {}, idioma)
      .pipe(take(1))
      .subscribe((titulo) => (this.document.title = titulo));
  }

  private inicial(): Idioma {
    let guardado: string | null = null;
    try {
      guardado = localStorage.getItem(STORAGE_KEY);
    } catch {
      guardado = null;
    }
    if (esIdioma(guardado)) return guardado;
    return navigator.language?.toLowerCase().startsWith('en') ? 'en' : 'es';
  }
}

function esIdioma(valor: string | null): valor is Idioma {
  return valor === 'es' || valor === 'en';
}
