import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';

import { CarreraService } from '../../core/api/carrera.service';
import { InscripcionService } from '../../core/api/inscripcion.service';
import { UsuarioService } from '../../core/api/usuario.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { CarreraResponse } from '../../core/models/carrera.model';
import { ResultadoCarreraRequest } from '../../core/models/resultado.model';

interface FilaResultado {
  usuarioId: number;
  piloto: string;
  posicion: number;
  tiempo: string;
  vueltaRapida: string;
  poles: boolean;
  finalizo: boolean;
  elo: number;
  sr: number;
}

@Component({
  selector: 'app-admin-resultados',
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTableModule,
  ],
  templateUrl: './admin-resultados.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminResultadosComponent {
  private readonly http = inject(HttpClient);
  private readonly carreraService = inject(CarreraService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly carreras = signal<CarreraResponse[]>([]);
  readonly filas = signal<FilaResultado[]>([]);
  readonly cargandoFilas = signal(false);

  readonly carreraSeleccionada = signal<number | null>(null);
  readonly jsonSesion = signal('');

  readonly columnas = ['piloto', 'posicion', 'tiempo', 'vueltaRapida', 'poles', 'finalizo', 'elo', 'sr'];

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.carreras.set(await firstValueFrom(this.carreraService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async cargarInscriptos() {
    const carreraId = this.carreraSeleccionada();
    if (!carreraId) {
      return;
    }
    this.cargandoFilas.set(true);
    try {
      const [inscripciones, usuarios] = await Promise.all([
        firstValueFrom(this.inscripcionService.deCarrera(carreraId)),
        firstValueFrom(this.usuarioService.listar()),
      ]);
      const nombres = new Map(usuarios.map((u) => [u.id, u.nombrePiloto ?? u.email]));
      this.filas.set(
        inscripciones
          .filter((i) => i.estado !== 'CANCELADA')
          .map((i, index) => ({
            usuarioId: i.usuarioId,
            piloto: nombres.get(i.usuarioId) ?? `Piloto #${i.usuarioId}`,
            posicion: index + 1,
            tiempo: '',
            vueltaRapida: '',
            poles: false,
            finalizo: true,
            elo: 0,
            sr: 0,
          })),
      );
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargandoFilas.set(false);
    }
  }

  private tiempoAMs(texto: string): number | null {
    const match = texto.match(/^(\d+):([0-5]\d)\.(\d{3})$/);
    if (!match) {
      return null;
    }
    const m = Number(match[1]);
    const s = Number(match[2]);
    const ms = Number(match[3]);
    return m * 60000 + s * 1000 + ms;
  }

  async guardarResultados() {
    const carreraId = this.carreraSeleccionada();
    if (!carreraId) {
      return;
    }
    const resultados: ResultadoCarreraRequest[] = [];
    for (const fila of this.filas()) {
      const tiempo = this.tiempoAMs(fila.tiempo);
      const vuelta = this.tiempoAMs(fila.vueltaRapida);
      if (tiempo === null && fila.finalizo) {
        this.errorHandler.handle(null, `Tiempo invalido para ${fila.piloto}. Usa el formato m:ss.mmm`);
        return;
      }
      resultados.push({
        carreraId,
        usuarioId: fila.usuarioId,
        posicionFinal: fila.posicion,
        tiempoTotal: tiempo,
        vueltaRapida: vuelta,
        poles: fila.poles,
        finalizo: fila.finalizo,
        eloGanado: fila.elo,
        srGanado: fila.sr,
      });
    }
    if (resultados.length === 0) {
      this.errorHandler.handle(null, 'Carga los inscriptos de una carrera primero');
      return;
    }
    this.guardando.set(true);
    try {
      await firstValueFrom(
        this.http.post('/api/resultados/cargar', { carreraId, resultados }),
      );
      this.errorHandler.exito('Resultados cargados');
      this.filas.set([]);
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async importarSesion() {
    const carreraId = this.carreraSeleccionada();
    if (!carreraId || !this.jsonSesion().trim()) {
      this.errorHandler.handle(null, 'Selecciona una carrera y pega el JSON de la sesion');
      return;
    }
    let sesion: unknown;
    try {
      sesion = JSON.parse(this.jsonSesion());
    } catch {
      this.errorHandler.handle(null, 'El JSON de la sesion es invalido');
      return;
    }
    this.guardando.set(true);
    try {
      const resultado = await firstValueFrom(
        this.http.post<{ tipo: string; estado: string }>(
          '/api/sesiones/importar',
          sesion,
          { params: { carreraId } },
        ),
      );
      this.errorHandler.exito(
        `Sesion procesada (${resultado.tipo}, estado ${resultado.estado})`,
      );
      this.jsonSesion.set('');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  trackFila(_index: number, fila: FilaResultado): number {
    return fila.usuarioId;
  }
}
