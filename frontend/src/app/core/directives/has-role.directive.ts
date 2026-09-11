import { Directive, Input, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { Rol } from '../models/models';
import { AuthService } from '../services/auth.service';

/**
 * Oculta el elemento y su contenido si el usuario NO tiene alguno de los roles dados.
 * Uso: <div *appHasRole="'ADMIN'">…</div> · <div *appHasRole="['ADMIN','COMISARIO']">…</div>
 */
@Directive({ selector: '[appHasRole]', standalone: true })
export class HasRoleDirective {
  @Input('appHasRole') set roles(r: Rol | Rol[]) {
    const list = Array.isArray(r) ? r : [r];
    if (this.auth.hasRole(...list)) {
      if (!this.view) this.view = this.vcr.createEmbeddedView(this.tpl);
    } else {
      this.view?.destroy();
      this.view = null;
    }
  }

  private view: ReturnType<ViewContainerRef['createEmbeddedView']> | null = null;
  private readonly auth = inject(AuthService);
  private readonly tpl = inject(TemplateRef<unknown>);
  private readonly vcr = inject(ViewContainerRef);
}