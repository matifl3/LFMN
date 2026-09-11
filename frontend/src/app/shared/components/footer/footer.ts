import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="site-footer">
      <div class="container-wide footer-grid">
        <div class="footer-col">
          <div class="brand">
            <img src="logo.png" alt="" class="brand-logo" />
            <span class="brand-name">LBM Nacional<small>Late Brake Motorsport</small></span>
          </div>
          <p class="text-terciario" style="font-size: var(--fs-sm); max-width: 30ch">
            Liga argentina de sim racing. Carreras semanales, categorías por Elo y Safety Rating.
          </p>
        </div>
        <div class="footer-col">
          <h4>Navegación</h4>
          <a routerLink="/">Inicio</a>
          <a routerLink="/carreras">Carreras</a>
          <a routerLink="/campeonato">Campeonato</a>
          <a routerLink="/categorias">Categorías</a>
          <a routerLink="/incidentes">Incidentes</a>
          <a routerLink="/setups">Setups</a>
          <a routerLink="/logros">Logros</a>
        </div>
        <div class="footer-col">
          <h4>Cuenta</h4>
          <a [routerLink]="auth.autenticado() ? '/mi-perfil' : '/auth'">Mi perfil</a>
          <a routerLink="/notificaciones">Notificaciones</a>
          @if (auth.esModerador()) {
            <a routerLink="/admin">Panel admin</a>
          }
        </div>
      </div>
      <div
        class="container-wide"
        style="margin-top: var(--sp-6); padding-top: var(--sp-4); border-top: 1px solid var(--borde-hair); display: flex; justify-content: space-between; flex-wrap: wrap; gap: var(--sp-2)"
      >
        <span class="text-terciario" style="font-size: var(--fs-sm)">© 2026 LBM Nacional — Late Brake Motorsport</span>
        <span class="text-terciario" style="font-size: var(--fs-sm)">Temporada 2026</span>
      </div>
    </footer>
  `,
})
export class Footer {
  readonly auth = inject(AuthService);
}