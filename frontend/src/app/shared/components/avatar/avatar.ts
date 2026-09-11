import { Component, computed, input } from '@angular/core';
import { iniciales } from '../../../core/utils/formato';

export type AvatarUser = { fotoPerfil?: string; nombrePiloto?: string; email?: string } | null | undefined;

@Component({
  selector: 'app-avatar',
  standalone: true,
  template: `
    @if (user()?.fotoPerfil) {
      <img class="avatar" [src]="user()!.fotoPerfil!" [width]="size()" [height]="size()" alt="" referrerpolicy="no-referrer" />
    } @else {
      <span class="avatar avatar-initials" [style.width.px]="size()" [style.height.px]="size()" [style.font-size.px]="size() / 2.4">
        {{ ini() }}
      </span>
    }
  `,
})
export class Avatar {
  readonly user = input<AvatarUser>(null);
  readonly size = input(38);
  readonly ini = computed(() => iniciales(this.user() as { nombrePiloto?: string; email?: string } | null | undefined));
}