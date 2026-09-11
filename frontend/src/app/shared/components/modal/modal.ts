import { Component, HostListener, contentChildren, input, output } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    @if (abierto()) {
      <div class="modal-overlay" (click)="cerrarConOverlay($event)">
        <div class="modal-panel" (click)="$event.stopPropagation()">
          <div class="modal-header">
            @if (titulo()) {
              <h3>{{ titulo() }}</h3>
            } @else {
              <ng-content select="[app-modal-titulo]" />
            }
            <button type="button" class="icon-btn modal-close" aria-label="Cerrar" (click)="cerrar()">✕</button>
          </div>
          <ng-content />
        </div>
      </div>
    }
  `,
})
export class Modal {
  readonly abierto = input(false);
  readonly titulo = input<string | null>(null);
  readonly clickFueraCierra = input(true);
  readonly cerrado = output<void>();

  private children = contentChildren((): Element | null => null);

  cerrar(): void {
    this.cerrado.emit();
  }

  cerrarConOverlay(e: MouseEvent): void {
    if (this.clickFueraCierra()) this.cerrado.emit();
  }

  @HostListener('document:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if (e.key === 'Escape' && this.abierto()) this.cerrado.emit();
  }
}