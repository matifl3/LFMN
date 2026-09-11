import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-rank-badge',
  standalone: true,
  template: `<span class="rank-badge {{ clase() }}">{{ posicion() }}</span>`,
})
export class RankBadge {
  readonly posicion = input<number | string | null | undefined>(undefined);
  readonly size = input(26);

  readonly clase = computed(() => {
    const p = this.posicion() as number | string | null | undefined;
    if (p === 1 || p === '1') return 'gold';
    if (p === 2 || p === '2') return 'silver';
    if (p === 3 || p === '3') return 'bronze';
    return '';
  });
}