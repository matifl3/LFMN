import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/auth/auth.service';
import { NotificacionService } from '../../core/api/notificacion.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { NotificacionResponse } from '../../core/models/notificacion.model';

@Component({
  selector: 'app-notificaciones',
  imports: [DatePipe, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './notificaciones.component.html',
  styleUrl: './notificaciones.component.scss',
})
export class NotificacionesComponent {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly notificacionService = inject(NotificacionService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly notificaciones = signal<NotificacionResponse[]>([]);

  private usuarioId = 0;

  constructor() {
    const usuario = this.auth.usuario();
    if (!usuario) {
      void this.router.navigate(['/login']);
      return;
    }
    this.usuarioId = usuario.id;
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.notificaciones.set(
        await firstValueFrom(this.notificacionService.porUsuario(this.usuarioId)),
      );
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async marcarLeida(notificacion: NotificacionResponse) {
    if (notificacion.leida) {
      return;
    }
    try {
      await firstValueFrom(this.notificacionService.marcarLeida(notificacion.id));
      this.notificaciones.update((list) =>
        list.map((n) => (n.id === notificacion.id ? { ...n, leida: true } : n)),
      );
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async marcarTodas() {
    try {
      await firstValueFrom(this.notificacionService.marcarTodasLeidas(this.usuarioId));
      this.notificaciones.update((list) => list.map((n) => ({ ...n, leida: true })));
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async eliminar(id: number) {
    try {
      await firstValueFrom(this.notificacionService.eliminar(id));
      this.notificaciones.update((list) => list.filter((n) => n.id !== id));
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  ir(notificacion: NotificacionResponse) {
    if (!notificacion.leida) {
      void this.marcarLeida(notificacion);
    }
    if (notificacion.link) {
      void this.router.navigateByUrl(notificacion.link);
    }
  }
}
