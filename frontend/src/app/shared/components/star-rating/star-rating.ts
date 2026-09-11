import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  template: `
    @if (puntaje() === null || puntaje() === undefined) {
      <span class="text-terciario mono" style="font-size: var(--fs-2xs)">Sin calificar</span>
    } @else {
      <span class="stars" [title]="puntaje()!.toFixed(1)">
        @for (i of [1, 2, 3, 4, 5]; track i) {
          <span [class.star-empty]="i > llenas()">★</span>
        }
      </span>
    }
  `,
})
export class StarRating {
  readonly puntaje = input<number | null | undefined>(null);
  readonly llenas = computed(() => Math.round(this.puntaje() ?? 0));
}