import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';

import { SetupService } from '../../core/api/setup.service';
import { UsuarioService } from '../../core/api/usuario.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { SetupResponse } from '../../core/models/setup.model';

@Component({
  selector: 'app-setups-list',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
  ],
  templateUrl: './setups-list.component.html',
  styleUrl: './setups-list.component.scss',
})
export class SetupsListComponent {
  private readonly setupService = inject(SetupService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly setups = signal<SetupResponse[]>([]);
  readonly autores = signal<Record<number, string>>({});

  circuito = '';
  vehiculo = '';

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [setups, usuarios] = await Promise.all([
        firstValueFrom(this.setupService.listar()),
        firstValueFrom(this.usuarioService.listar()),
      ]);
      this.setups.set(setups);
      this.autores.set(Object.fromEntries(usuarios.map((u) => [u.id, u.nombrePiloto ?? u.email])));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async buscar() {
    this.cargando.set(true);
    try {
      this.setups.set(
        await firstValueFrom(
          this.setupService.buscar(this.circuito?.trim() || undefined, this.vehiculo?.trim() || undefined),
        ),
      );
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  autor(nombre: string, autorId: number): string {
    return `${nombre} - ${this.autores()[autorId] ?? 'Autor'}`;
  }

  estrellas(promedio: number | null): string {
    if (promedio === null || promedio === undefined) {
      return '-';
    }
    return promedio.toFixed(1);
  }
}
