import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { UsuarioService } from '../../core/api/usuario.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { UsuarioResponse } from '../../core/models/usuario.model';
import { Rol } from '../../core/models/common.model';

@Component({
  selector: 'app-admin-usuarios',
  imports: [
    DatePipe,
    RouterLink,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './admin-usuarios.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminUsuariosComponent {
  private readonly usuarioService = inject(UsuarioService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly roles = [Rol.USUARIO, Rol.ADMIN, Rol.COMISARIO];

  readonly columnas = ['nombre', 'email', 'elo', 'sr', 'rol', 'registro', 'acciones'];

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.usuarios.set(await firstValueFrom(this.usuarioService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async cambiarRol(usuario: UsuarioResponse, rol: Rol) {
    try {
      await firstValueFrom(this.usuarioService.cambiarRol(usuario.id, rol));
      this.usuarios.update((list) =>
        list.map((u) => (u.id === usuario.id ? { ...u, rol } : u)),
      );
      this.errorHandler.exito('Rol actualizado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async eliminar(usuario: UsuarioResponse) {
    if (!window.confirm(`Seguro que queres eliminar a ${usuario.nombrePiloto ?? usuario.email}?`)) {
      return;
    }
    try {
      await firstValueFrom(this.usuarioService.eliminar(usuario.id));
      this.usuarios.update((list) => list.filter((u) => u.id !== usuario.id));
      this.errorHandler.exito('Usuario eliminado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }
}
