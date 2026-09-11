import { Component, computed, input } from '@angular/core';
import { EstadoChipPipe } from '../../../core/pipes/estado-chip.pipe';

@Component({
  selector: 'app-chip',
  standalone: true,
  template: `<span class="chip {{ info().clase }}">{{ info().label }}</span>`,
})
export class Chip {
  readonly estado = input<string | null | undefined>(undefined);
  readonly tipo = input('generic');
  readonly overrideLabel = input<string | null>(null);
  readonly overrideClass = input<string | null>(null);

  private readonly pipe = new EstadoChipPipe();

  readonly info = computed(() => {
    const base = this.pipe.transform(this.estado(), this.tipo());
    return {
      clase: this.overrideClass() || base.clase,
      label: this.overrideLabel() || base.label,
    };
  });
}