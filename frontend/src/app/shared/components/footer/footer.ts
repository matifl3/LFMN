import { TranslocoPipe } from '@jsverse/transloco';
import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [TranslocoPipe, RouterLink],
  template: `
    <footer class="site-footer">
      <div class="container-wide footer-grid">
        <div class="footer-col">
          <div class="brand">
            <img src="logo.png" alt="" class="brand-logo" />
            <span class="brand-name">LBM Nacional<small>Late Brake Motorsport</small></span>
          </div>
          <p class="text-terciario" style="font-size: var(--fs-sm); max-width: 30ch">
            {{ 'comun.footer.descripcion' | transloco }}
          </p>
        </div>
        <div class="footer-col">
          <h4>{{ 'comun.footer.navegacion' | transloco }}</h4>
          <a routerLink="/">{{ 'comun.footer.inicio' | transloco }}</a>
          <a routerLink="/carreras">{{ 'nav.carreras' | transloco }}</a>
          <a routerLink="/campeonato">{{ 'nav.campeonato' | transloco }}</a>
          <a routerLink="/categorias">{{ 'nav.categorias' | transloco }}</a>
          <a routerLink="/incidentes">{{ 'nav.incidentes' | transloco }}</a>
          <a routerLink="/setups">{{ 'nav.setups' | transloco }}</a>
          <a routerLink="/logros">{{ 'nav.logros' | transloco }}</a>
        </div>
        <div class="footer-col">
          <h4>{{ 'comun.footer.cuenta' | transloco }}</h4>
          <a [routerLink]="miPerfil()">{{ 'header.miPerfil' | transloco }}</a>
          <a routerLink="/notificaciones">{{ 'header.notificaciones' | transloco }}</a>
          @if (auth.esModerador()) {
            <a routerLink="/admin">{{ 'comun.footer.panelAdmin' | transloco }}</a>
          }
        </div>
      </div>
      <div
        class="container-wide"
        style="margin-top: var(--sp-6); padding-top: var(--sp-4); border-top: 1px solid var(--borde-hair); display: flex; justify-content: space-between; flex-wrap: wrap; gap: var(--sp-2)"
      >
        <span class="text-terciario" style="font-size: var(--fs-sm)"
          >© 2026 LBM Nacional — Late Brake Motorsport</span
        >
        <span class="text-terciario" style="font-size: var(--fs-sm)">{{
          'comun.footer.temporada' | transloco
        }}</span>
      </div>
    </footer>
  `,
})
export class Footer {
  readonly auth = inject(AuthService);

  readonly miPerfil = computed(() => {
    const id = this.auth.user()?.id;
    return id ? ['/perfil', id] : '/auth';
  });
}
