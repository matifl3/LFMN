import { Component, input, output } from '@angular/core';
import { Modal } from '../modal/modal';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [Modal],
  template: `
    <app-modal [abierto]="abierto()" [titulo]="titulo()" (cerrado)="cancelar()">
      <p class="text-secondary">{{ mensaje() }}</p>
      <div class="flex" style="justify-content:flex-end; gap: var(--sp-3); margin-top: var(--sp-5)">
        <button type="button" class="btn btn-ghost" (click)="cancelar()">Cancelar</button>
        <button type="button" class="btn btn-primary" (click)="confirmar()">{{ confirmarTexto() }}</button>
      </div>
    </app-modal>
  `,
})
export class ConfirmDialog {
  readonly abierto = input(false);
  readonly titulo = input('¿Confirmar?');
  readonly mensaje = input('');
  readonly confirmarTexto = input('Confirmar');
  readonly aceptado = output<void>();
  readonly cancelado = output<void>();

  confirmar(): void {
    this.aceptado.emit();
  }

  cancelar(): void {
    this.cancelado.emit();
  }
}