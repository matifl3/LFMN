import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-locked-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="locked-state">
      <span class="ms-icon locked-icon" aria-hidden="true">lock</span>
      <h2 class="locked-title">{{ titulo() }}</h2>
      <p class="text-secondary locked-text">{{ detalle() }}</p>
      <a routerLink="/auth" class="btn btn-primary btn-lg">{{ accion() }}</a>
    </div>
  `,
})
export class LockedState {
  readonly titulo = input('Iniciá sesión para ver esta sección');
  readonly detalle = input('Necesitás una cuenta de piloto para acceder a este contenido.');
  readonly accion = input('Ir al Login');
}