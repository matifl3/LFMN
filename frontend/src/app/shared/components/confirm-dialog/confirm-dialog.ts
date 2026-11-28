import { TranslocoPipe } from '@jsverse/transloco';
import { Component, input, output } from '@angular/core';
import { Modal } from '../modal/modal';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [Modal, TranslocoPipe],
  template: `
    <app-modal
      [abierto]="abierto()"
      [titulo]="titulo() ?? ('comun.confirmarPregunta' | transloco)"
      (cerrado)="cancelar()"
    >
      <p class="text-secondary">{{ mensaje() }}</p>
      <div class="flex" style="justify-content:flex-end; gap: var(--sp-3); margin-top: var(--sp-5)">
        <button type="button" class="btn btn-ghost" (click)="cancelar()">
          {{ 'comun.cancelar' | transloco }}
        </button>
        <button type="button" class="btn btn-primary" (click)="confirmar()">
          {{ confirmarTexto() ?? ('comun.confirmar' | transloco) }}
        </button>
      </div>
    </app-modal>
  `,
})
export class ConfirmDialog {
  readonly abierto = input(false);
  readonly titulo = input<string | null>(null);
  readonly mensaje = input('');
  readonly confirmarTexto = input<string | null>(null);
  readonly aceptado = output<void>();
  readonly cancelado = output<void>();

  confirmar(): void {
    this.aceptado.emit();
  }

  cancelar(): void {
    this.cancelado.emit();
  }
}
