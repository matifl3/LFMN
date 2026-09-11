import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toast-root" aria-live="polite">
      @for (t of toast.items(); track t.id) {
        <div class="toast {{ t.tipo }}" role="status">
          {{ t.mensaje }}
          <span class="toast-bar" [style.animation-duration]="t.duracion + 'ms'"></span>
        </div>
      }
    </div>
  `,
})
export class ToastContainer {
  readonly toast = inject(ToastService);
}