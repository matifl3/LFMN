import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state">
      <span class="ms-icon empty-state-icon" aria-hidden="true">{{ icono() }}</span>
      @if (titulo()) {
        <h4 class="empty-state-title">{{ titulo() }}</h4>
      }
      <p class="empty-state-text">{{ texto() }}</p>
      <ng-content />
    </div>
  `,
})
export class EmptyState {
  readonly texto = input('No hay elementos.');
  readonly titulo = input<string | null>(null);
  readonly icono = input('inbox');
}