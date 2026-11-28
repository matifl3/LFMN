import { TranslocoPipe } from '@jsverse/transloco';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-locked-state',
  standalone: true,
  imports: [TranslocoPipe, RouterLink],
  template: `
    <div class="locked-state">
      <span class="ms-icon locked-icon" aria-hidden="true">lock</span>
      <h2 class="locked-title">{{ titulo() ?? ('comun.bloqueado.titulo' | transloco) }}</h2>
      <p class="text-secondary locked-text">
        {{ detalle() ?? ('comun.bloqueado.detalle' | transloco) }}
      </p>
      <a routerLink="/auth" class="btn btn-primary btn-lg">{{
        accion() ?? ('comun.bloqueado.accion' | transloco)
      }}</a>
    </div>
  `,
})
export class LockedState {
  readonly titulo = input<string | null>(null);
  readonly detalle = input<string | null>(null);
  readonly accion = input<string | null>(null);
}
