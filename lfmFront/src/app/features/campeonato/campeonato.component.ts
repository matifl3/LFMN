import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';

import { CampeonatoService } from '../../core/api/campeonato.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { CampeonatoResponse, TablaPosicionResponse } from '../../core/models/campeonato.model';

@Component({
  selector: 'app-campeonato',
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatChipsModule,
  ],
  templateUrl: './campeonato.component.html',
  styleUrl: './campeonato.component.scss',
})
export class CampeonatoComponent {
  private readonly campeonatoService = inject(CampeonatoService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly cargandoTabla = signal(false);
  readonly campeonatos = signal<CampeonatoResponse[]>([]);
  readonly seleccionado = signal<CampeonatoResponse | null>(null);
  readonly tabla = signal<TablaPosicionResponse[]>([]);

  readonly columnas = ['posicion', 'piloto', 'puntos'];

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.campeonatos.set(await firstValueFrom(this.campeonatoService.listar()));
      const activo = this.campeonatos().find((c) => c.estado === 'ACTIVO') ?? this.campeonatos()[0];
      if (activo) {
        await this.seleccionar(activo);
      }
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async seleccionar(campeonato: CampeonatoResponse) {
    this.seleccionado.set(campeonato);
    this.cargandoTabla.set(true);
    try {
      this.tabla.set(await firstValueFrom(this.campeonatoService.tabla(campeonato.id)));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargandoTabla.set(false);
    }
  }
}
