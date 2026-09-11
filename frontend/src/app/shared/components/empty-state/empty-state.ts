import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state">
      @if (icono()) {
        <div class="category-icon">{{ icono() }}</div>
      }
      <p>{{ texto() }}</p>
      <ng-content />
    </div>
  `,
})
export class EmptyState {
  readonly texto = input('No hay elementos.');
  readonly icono = input<string | null>(null);
}