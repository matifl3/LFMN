import { Component, inject } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { IDIOMAS, IdiomaService } from '../../../core/services/idioma.service';

/** Selector ES | EN: va en el header, en el menú móvil y en la pantalla de ingreso. */
@Component({
  selector: 'app-selector-idioma',
  standalone: true,
  imports: [TranslocoPipe],
  template: `
    <div class="selector-idioma" role="group" [attr.aria-label]="'idioma.selector' | transloco">
      @for (id of idiomas; track id) {
        <button
          type="button"
          [class.active]="idioma.actual() === id"
          [attr.aria-pressed]="idioma.actual() === id"
          [attr.lang]="id"
          [attr.title]="'idioma.' + id | transloco"
          (click)="idioma.cambiar(id)"
        >
          {{ id.toUpperCase() }}
        </button>
      }
    </div>
  `,
  styles: `
    .selector-idioma {
      display: inline-flex;
      align-items: center;
      border: 1px solid var(--borde-metal);
      border-radius: var(--r-pill);
      padding: 2px;
      gap: 2px;
    }
    button {
      min-width: 34px;
      height: 28px;
      padding-inline: var(--sp-2);
      border: 0;
      border-radius: var(--r-pill);
      background: transparent;
      color: var(--texto-secundario);
      font-family: var(--font-display);
      font-size: var(--fs-xs);
      font-weight: 600;
      letter-spacing: 0.04em;
      cursor: pointer;
      transition:
        color var(--dur-base) var(--ease),
        background-color var(--dur-base) var(--ease);
    }
    button:hover {
      color: var(--texto-principal);
    }
    button.active {
      background: var(--bg-superficie);
      color: var(--texto-principal);
    }
    button:focus-visible {
      outline: 2px solid var(--lbm-rojo);
      outline-offset: 2px;
    }
  `,
})
export class SelectorIdioma {
  readonly idioma = inject(IdiomaService);
  readonly idiomas = IDIOMAS;
}
